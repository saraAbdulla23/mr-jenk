package com.product_service.service;

import com.product_service.kafka.ProductEventPublisher;
import com.product_service.model.Product;
import com.product_service.repository.ProductRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final ProductEventPublisher eventPublisher;

    public ProductService(ProductRepository repository, ProductEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasRole('SELLER')")
    public Product createProduct(Product product, String userId) {
        product.setUserId(userId);
        if (product.getImageUrls() == null) {
            product.setImageUrls(new ArrayList<>());
        }
        Product saved = repository.save(product);
        eventPublisher.publishProductCreated(saved);
        return saved;
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    @PreAuthorize("hasRole('SELLER')")
    public List<Product> getProductsByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @PreAuthorize("hasRole('SELLER')")
    public Product updateProduct(String userId, String productId, Product updatedProduct) {
        Product existing = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existing.getUserId().equals(userId)) {
            throw new SecurityException("User cannot modify this product");
        }

        existing.setName(updatedProduct.getName());
        existing.setPrice(updatedProduct.getPrice());
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setDescription(updatedProduct.getDescription());

        Product saved = repository.save(existing);
        eventPublisher.publishProductUpdated(saved);
        return saved;
    }

    @PreAuthorize("hasRole('SELLER')")
    public void deleteProduct(String userId, String productId) {
        Product existing = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existing.getUserId().equals(userId)) {
            throw new SecurityException("User cannot delete this product");
        }

        repository.deleteById(productId);
        eventPublisher.publishProductDeleted(existing);
    }

    public Product addImage(String productId, String userId, String imageUrl) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getUserId().equals(userId)) {
            throw new SecurityException("User cannot modify this product");
        }

        if (product.getImageUrls() == null) {
            product.setImageUrls(new ArrayList<>());
        }

        if (!product.getImageUrls().contains(imageUrl)) {
            product.getImageUrls().add(imageUrl);
        }

        Product saved = repository.save(product);
        eventPublisher.publishProductUpdated(saved);
        return saved;
    }

    public Product removeImage(String productId, String userId, String imageUrl) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getUserId().equals(userId)) {
            throw new SecurityException("User cannot modify this product");
        }

        if (product.getImageUrls() != null) {
            product.getImageUrls().remove(imageUrl);
        }

        Product saved = repository.save(product);
        eventPublisher.publishProductUpdated(saved);
        return saved;
    }

    // ✅ NEW: used by media service to check ownership
    public Product getProductById(String productId) {
        return repository.findById(productId).orElse(null);
    }
}
