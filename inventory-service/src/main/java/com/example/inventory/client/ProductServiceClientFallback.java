package com.example.inventory.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceClientFallback implements ProductServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceClientFallback.class);

    @Override
    public ResponseEntity<Object> createProduct(ProductCreateRequest product) {
        logger.warn("⚠️ Product service is unavailable. Fallback triggered for product: {}", product.getName());
        // Return a fallback response indicating the service is unavailable
        return ResponseEntity.status(503).body("Product service unavailable");
    }
}