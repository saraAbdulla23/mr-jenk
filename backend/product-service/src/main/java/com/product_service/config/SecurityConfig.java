package com.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!test") // ✅ THIS IS THE KEY
public class SecurityConfig {

    // 🔐 Must match the secret used by Auth Service / API Gateway
    private static final String JWT_SECRET =
            "KfTpBkd4/IZ8utP7ia3aZ8z7AM92lQmYJvgqxOGNR5o=";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ❌ Do NOT configure CORS here — API Gateway handles it
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // =========================
                // PUBLIC READ-ONLY ENDPOINTS
                // =========================
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                // =========================
                // EVERYTHING ELSE REQUIRES AUTH
                // =========================
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * 🔑 Maps JWT "role" claim → Spring Security authorities
     * Example:
     *   "role": "ROLE_SELLER"
     * becomes:
     *   GrantedAuthority("ROLE_SELLER")
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix(""); // token already contains ROLE_

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return jwtConverter;
    }

    /**
     * 🔐 JWT decoder using shared HS256 secret
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(JWT_SECRET);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
