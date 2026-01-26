package com.product_service.controller;

import com.product_service.model.Product;
import com.product_service.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<?> createProduct(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody Product productRequest) {
        try {
            String userId = jwt.getClaimAsString("userId");
            productRequest.setUserId(userId);

            Product createdProduct = productService.createProduct(productRequest, userId);
            return ResponseEntity.ok(createdProduct);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // ================= READ =================
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyProducts(@AuthenticationPrincipal Jwt jwt) {
        try {
            String userId = jwt.getClaimAsString("userId");
            List<Product> products = productService.getProductsByUserId(userId);
            return ResponseEntity.ok(products);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/{productId}/owner")
    public ResponseEntity<?> getProductOwner(@PathVariable String productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product not found"));
        }
        return ResponseEntity.ok(Map.of("ownerId", product.getUserId()));
    }

    // ================= UPDATE =================
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId,
            @Valid @RequestBody Product updatedProduct) {
        try {
            String userId = jwt.getClaimAsString("userId");
            Product product = productService.updateProduct(userId, productId, updatedProduct);
            return ResponseEntity.ok(product);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ================= DELETE =================
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId) {
        try {
            String userId = jwt.getClaimAsString("userId");
            productService.deleteProduct(userId, productId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // ================= IMAGES =================
    @PostMapping("/{productId}/images")
    public ResponseEntity<?> addImage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId,
            @RequestBody String imageUrl) {
        try {
            String userId = jwt.getClaimAsString("userId");
            Product product = productService.addImage(productId, userId, imageUrl);
            return ResponseEntity.ok(product);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{productId}/images")
    public ResponseEntity<?> removeImage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId,
            @RequestBody String imageUrl) {
        try {
            String userId = jwt.getClaimAsString("userId");
            Product product = productService.removeImage(productId, userId, imageUrl);
            return ResponseEntity.ok(product);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
