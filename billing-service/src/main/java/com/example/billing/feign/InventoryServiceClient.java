package com.example.billing.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "inventory-service", fallback = InventoryServiceFallback.class)
public interface InventoryServiceClient {

    @GetMapping("/api/items")
    List<Map<String, Object>> getAllItems();

    @GetMapping("/api/items/{id}")
    Map<String, Object> getItemById(@PathVariable("id") Long id);

    @PostMapping("/api/items")
    Map<String, Object> addItem(@RequestBody Map<String, Object> item);

    @PutMapping("/api/items/{id}/stock")
    Map<String, Object> updateStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);

    @GetMapping("/api/items/{id}/stock/check")
    Map<String, Object> checkStock(@PathVariable("id") Long id, @RequestParam("requiredQuantity") int requiredQuantity);

    @PutMapping("/api/items/{id}/stock/add")
    Map<String, Object> addStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);

    @PutMapping("/api/items/{id}")
    Map<String, Object> updateItem(@PathVariable("id") Long id, @RequestBody Map<String, Object> item);

    @DeleteMapping("/api/items/{id}")
    void deleteItem(@PathVariable("id") Long id);
}