package com.example.billing.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service", fallback = ProductServiceFallback.class)
public interface ProductServiceClient {

    @GetMapping("/api/products")
    List<Map<String, Object>> getAllProducts();

    @GetMapping("/api/products/{id}")
    Map<String, Object> getProductById(@PathVariable("id") Long id);

    @GetMapping("/api/products/category/{category}")
    List<Map<String, Object>> getProductsByCategory(@PathVariable("category") String category);

    @GetMapping("/api/products/categories")
    List<String> getAllCategories();

    @GetMapping("/api/products/search")
    List<Map<String, Object>> searchProducts(@RequestParam("name") String name);

    @GetMapping("/api/products/low-stock")
    List<Map<String, Object>> getLowStockProducts(@RequestParam(value = "threshold", defaultValue = "10") int threshold);

    @PostMapping("/api/products")
    Map<String, Object> createProduct(@RequestBody Map<String, Object> product);

    @PutMapping("/api/products/{id}")
    Map<String, Object> updateProduct(@PathVariable("id") Long id, @RequestBody Map<String, Object> product);

    @PutMapping("/api/products/{id}/stock")
    Map<String, Object> updateStock(@PathVariable("id") Long id, @RequestParam("stock") int stock);

    @PutMapping("/api/products/{id}/reduce-stock")
    Map<String, Object> reduceStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);

    @DeleteMapping("/api/products/{id}")
    void deleteProduct(@PathVariable("id") Long id);

    @GetMapping("/api/products/stats")
    Map<String, Object> getProductStats();
}