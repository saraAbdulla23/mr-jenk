package backend.api_gateway.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import backend.api_gateway.service.JwtService;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // important! run before security checks
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationWebFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationWebFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        logger.debug("Filtering request for path: {}", path);

        // Allow public endpoints
        if (path.startsWith("/auth") || exchange.getRequest().getMethod().name().equals("OPTIONS")) {
            logger.debug("Public endpoint or OPTIONS request, skipping JWT check");
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header");
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            logger.warn("Invalid JWT token for path {}", path);
            return chain.filter(exchange);
        }

        String email = jwtService.getEmailFromToken(token);
        String role = jwtService.getRoleFromToken(token); // as-is, e.g., ROLE_SELLER
        logger.debug("JWT validated for user: {}, role: {}", email, role);

        var authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority(role))
        );

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}
