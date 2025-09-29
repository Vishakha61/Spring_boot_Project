package com.example.billing.feign;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ProductServiceFallback implements ProductServiceClient {

    @Override
    public List<Map<String, Object>> getAllProducts() {
        return getSampleProducts();
    }

    @Override
    public Map<String, Object> getProductById(Long id) {
        return getSampleProducts().stream()
                .filter(product -> product.get("id").equals(id.intValue()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Map<String, Object>> getProductsByCategory(String category) {
        return getSampleProducts().stream()
                .filter(product -> product.get("category").equals(category))
                .toList();
    }

    @Override
    public List<String> getAllCategories() {
        return List.of("Electronics", "Clothing", "Books", "Home & Garden");
    }

    @Override
    public List<Map<String, Object>> searchProducts(String name) {
        return getSampleProducts().stream()
                .filter(product -> ((String) product.get("name")).toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    @Override
    public List<Map<String, Object>> getLowStockProducts(int threshold) {
        return getSampleProducts().stream()
                .filter(product -> (Integer) product.get("stockQuantity") < threshold)
                .toList();
    }

    @Override
    public Map<String, Object> createProduct(Map<String, Object> product) {
        return Map.of("message", "Product service unavailable, product not created");
    }

    @Override
    public Map<String, Object> updateProduct(Long id, Map<String, Object> product) {
        return Map.of("message", "Product service unavailable, product not updated");
    }

    @Override
    public Map<String, Object> updateStock(Long id, int stock) {
        return Map.of("message", "Product service unavailable, stock not updated");
    }

    @Override
    public Map<String, Object> reduceStock(Long id, int quantity) {
        return Map.of("message", "Product service unavailable, stock not reduced");
    }

    @Override
    public void deleteProduct(Long id) {
        // Do nothing - service unavailable
    }

    @Override
    public Map<String, Object> getProductStats() {
        return Map.of(
            "totalProducts", 0,
            "activeProducts", 0,
            "categories", 0,
            "lowStockProducts", 0,
            "message", "Product service unavailable"
        );
    }

    private List<Map<String, Object>> getSampleProducts() {
        return List.of(
            Map.of(
                "id", 1,
                "name", "Gaming Laptop",
                "category", "Electronics",
                "description", "High-performance gaming laptop",
                "price", 75000.0,
                "stockQuantity", 5,
                "sku", "ELEGAL001",
                "isActive", true
            ),
            Map.of(
                "id", 2,
                "name", "Wireless Earbuds",
                "category", "Electronics",
                "description", "Premium wireless earbuds",
                "price", 8000.0,
                "stockQuantity", 20,
                "sku", "ELEWIR002",
                "isActive", true
            ),
            Map.of(
                "id", 3,
                "name", "Cotton T-Shirt",
                "category", "Clothing",
                "description", "Comfortable cotton t-shirt",
                "price", 800.0,
                "stockQuantity", 50,
                "sku", "CLCOTT003",
                "isActive", true
            ),
            Map.of(
                "id", 4,
                "name", "Programming Book",
                "category", "Books",
                "description", "Learn Java programming",
                "price", 1200.0,
                "stockQuantity", 15,
                "sku", "BOPRO004",
                "isActive", true
            ),
            Map.of(
                "id", 5,
                "name", "Plant Pot",
                "category", "Home & Garden",
                "description", "Ceramic plant pot",
                "price", 300.0,
                "stockQuantity", 8,
                "sku", "HOPLA005",
                "isActive", true
            )
        );
    }
}