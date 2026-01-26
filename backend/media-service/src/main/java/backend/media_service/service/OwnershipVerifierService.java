package backend.media_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OwnershipVerifierService {

    private static final Logger logger = LoggerFactory.getLogger(OwnershipVerifierService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Boolean> ownershipCache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final String PRODUCT_SERVICE_VERIFY_URL = "http://localhost:8085/api/products/{productId}/owner";

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secretKeyString;

    private SecretKey secretKey;

    public OwnershipVerifierService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    private void init() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKeyString);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ================= JWT HELPERS =================
    public String getUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Missing or invalid Authorization header");
        }
        String token = authorizationHeader.substring(7);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        String userId = claims.get("userId", String.class);
        logger.debug("Extracted userId from JWT: {}", userId);
        return userId;
    }

    /**
     * Checks if JWT contains the specified role.
     * Automatically handles "ROLE_" prefix.
     */
    public boolean hasRole(String authorizationHeader, String role) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) return false;
        String token = authorizationHeader.substring(7);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Check for multiple roles
        Object roles = claims.get("roles");
        if (roles instanceof Iterable<?>) {
            for (Object r : (Iterable<?>) roles) {
                if (roleMatches(r.toString(), role)) return true;
            }
        }

        // Check single role
        Object singleRole = claims.get("role");
        return singleRole != null && roleMatches(singleRole.toString(), role);
    }

    /**
     * Matches role string ignoring "ROLE_" prefix.
     */
    private boolean roleMatches(String jwtRole, String expectedRole) {
        if (jwtRole == null || expectedRole == null) return false;
        jwtRole = jwtRole.toUpperCase();
        expectedRole = expectedRole.toUpperCase();
        if (jwtRole.startsWith("ROLE_")) jwtRole = jwtRole.substring(5);
        return jwtRole.equals(expectedRole);
    }

    public boolean isSeller(String authorizationHeader) {
        return hasRole(authorizationHeader, "SELLER");
    }

    // ================= PRODUCT OWNERSHIP =================
    public boolean verifyOwnership(String productId, String userId, String authorizationHeader) {
        String cacheKey = productId + ":" + userId;

        if (ownershipCache.containsKey(cacheKey)) {
            logger.debug("Ownership cache hit for {}: {}", cacheKey, ownershipCache.get(cacheKey));
            return ownershipCache.get(cacheKey);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    PRODUCT_SERVICE_VERIFY_URL,
                    HttpMethod.GET,
                    entity,
                    Map.class,
                    productId
            );

            Map<String, Object> response = responseEntity.getBody();
            if (response == null || !response.containsKey("ownerId")) {
                logger.warn("Product service response missing ownerId for product {}", productId);
                ownershipCache.put(cacheKey, false);
                return false;
            }

            String ownerId = response.get("ownerId").toString();
            boolean isOwner = userId.equals(ownerId);

            logger.info("Verifying ownership for product {}: userId={}, ownerId={}, isOwner={}",
                    productId, userId, ownerId, isOwner);

            ownershipCache.put(cacheKey, isOwner);
            return isOwner;

        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Unauthorized request when verifying ownership for product {}: {}", productId, e.getMessage());
            ownershipCache.put(cacheKey, false);
            return false;
        } catch (Exception e) {
            logger.error("Failed to verify ownership for product {}: {}", productId, e.getMessage(), e);
            ownershipCache.put(cacheKey, false);
            return false;
        }
    }

    public boolean verifyOwnership(String productId, String userId) {
        throw new SecurityException("Must provide Authorization header for ownership verification");
    }

    // ================= KAFKA CORE =================
    private void reliablePublish(String topic, Map<String, String> message) {
        int MAX_RETRIES = 5;
        long RETRY_DELAY_MS = 2000;
        int attempts = 0;
        boolean sent = false;
        String payload;

        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Kafka message", e);
        }

        while (!sent && attempts < MAX_RETRIES) {
            try {
                kafkaTemplate.send(topic, payload).get();
                sent = true;
            } catch (Exception e) {
                attempts++;
                logger.warn("Kafka send failed (attempt {}/{}), retrying...", attempts, MAX_RETRIES);
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
        }

        if (!sent) {
            logger.error("Failed to send Kafka message after {} attempts: {}", MAX_RETRIES, payload);
        }
    }

    // ================= PRODUCT MEDIA EVENTS =================
    public void publishMediaUploaded(String productId, String userId, String imageUrl) {
        reliablePublish("media-uploaded-topic", Map.of("productId", productId, "userId", userId, "imageUrl", imageUrl));
    }

    public void publishMediaDeleted(String productId, String userId, String imageUrl) {
        reliablePublish("media-deleted-topic", Map.of("productId", productId, "userId", userId, "imageUrl", imageUrl));
    }

    // ================= AVATAR EVENTS =================
    public void publishAvatarUploaded(String userId, String avatarUrl) {
        reliablePublish("avatar-updated-topic", Map.of("userId", userId, "avatarUrl", avatarUrl == null ? "" : avatarUrl));
    }
}
