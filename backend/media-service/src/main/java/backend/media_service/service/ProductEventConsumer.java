package backend.media_service.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import backend.media_service.model.Media;
import backend.media_service.repository.MediaRepository;

@Service
public class ProductEventConsumer {

    private final List<Map<String, Object>> products = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;

    private final RestTemplate restTemplate = new RestTemplate(); // For calling Product Service

    // URL of Product Service to add image
    private final String PRODUCT_SERVICE_ADD_IMAGE_URL = "http://localhost:8085/api/products/{productId}/{userId}/images";

    public ProductEventConsumer(MediaRepository mediaRepository, FileStorageService fileStorageService) {
        this.mediaRepository = mediaRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Map<String, Object>> getProducts() {
        return products;
    }

    // ================= PRODUCT CREATED =================
    @KafkaListener(topics = "product-created-topic", groupId = "media-service-group")
    public void handleProductCreated(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            System.out.println("✅ Product created: " + event);
            products.add(event);
        } catch (Exception e) {
            System.err.println("❌ Failed to parse product-created event: " + e.getMessage());
        }
    }

    // ================= PRODUCT UPDATED =================
    @KafkaListener(topics = "product-updated-topic", groupId = "media-service-group")
    public void handleProductUpdated(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            System.out.println("✅ Product updated: " + event);

            products.removeIf(p -> p.get("productId").equals(event.get("productId")));

            Map<String, Object> updatedProduct = new HashMap<>();
            updatedProduct.put("productId", event.get("productId"));
            updatedProduct.put("name", event.get("name"));

            products.add(updatedProduct);
        } catch (Exception e) {
            System.err.println("❌ Failed to parse product-updated event: " + e.getMessage());
        }
    }

    // ================= PRODUCT DELETED =================
    @KafkaListener(topics = "product-deleted-topic", groupId = "media-service-group")
    public void handleProductDeleted(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            System.out.println("✅ Product deleted: " + event);

            String productId = (String) event.get("productId");
            products.removeIf(p -> p.get("productId").equals(productId));

            // delete all media related to this product
            List<Media> mediaList = mediaRepository.findByProductId(productId);
            for (Media media : mediaList) {
                try {
                    fileStorageService.deleteFileByUrl(media.getImagePath());
                    mediaRepository.delete(media);
                } catch (Exception e) {
                    System.err.println(
                        "❌ Error deleting media for product " + productId + ": " + e.getMessage()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to parse product-deleted event: " + e.getMessage());
        }
    }

    // ================= MEDIA UPLOADED =================
    @KafkaListener(topics = "media-uploaded-topic", groupId = "media-service-group")
    public void handleMediaUploaded(String message) {
        try {
            Map<String, String> event = objectMapper.readValue(message, Map.class);
            String productId = event.get("productId");
            String userId = event.get("userId");
            String imageUrl = event.get("imageUrl");

            System.out.println("✅ Media uploaded event received: " + event);

            // Call Product Service to add image URL
            try {
                restTemplate.postForEntity(
                        PRODUCT_SERVICE_ADD_IMAGE_URL,
                        "\"" + imageUrl + "\"", // Product Service expects raw string in body
                        Void.class,
                        productId,
                        userId
                );
                System.out.println("✅ Image URL sent to Product Service: " + imageUrl);
            } catch (Exception e) {
                System.err.println("❌ Failed to send image to Product Service: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to parse media-uploaded event: " + e.getMessage());
        }
    }
}
