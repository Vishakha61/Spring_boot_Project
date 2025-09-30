package com.example.billing.feign;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class InventoryServiceFallback implements InventoryServiceClient {

    @Override
    public List<Map<String, Object>> getAllItems() {
        return getSampleItems();
    }

    @Override
    public Map<String, Object> getItemById(Long id) {
        return getSampleItems().stream()
                .filter(item -> item.get("id").equals(id.intValue()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Map<String, Object> addItem(Map<String, Object> item) {
        return Map.of("message", "Inventory service unavailable, item not added");
    }

    @Override
    public Map<String, Object> updateStock(Long id, int quantity) {
        return Map.of("message", "Inventory service unavailable, stock not updated");
    }

    @Override
    public Map<String, Object> updateItem(Long id, Map<String, Object> item) {
        return Map.of("message", "Inventory service unavailable, item not updated");
    }

    @Override
    public void deleteItem(Long id) {
        // Do nothing - service unavailable
    }

    @Override
    public Map<String, Object> checkStock(Long id, int requiredQuantity) {
        return Map.of(
            "itemId", id,
            "itemName", "Unknown Item",
            "currentStock", 0,
            "requiredQuantity", requiredQuantity,
            "available", false,
            "status", "SERVICE_UNAVAILABLE",
            "message", "Inventory service unavailable"
        );
    }

    @Override
    public Map<String, Object> addStock(Long id, int quantity) {
        return Map.of(
            "message", "Inventory service unavailable, stock not added",
            "status", "SERVICE_UNAVAILABLE"
        );
    }

    private List<Map<String, Object>> getSampleItems() {
        return List.of(
            Map.of(
                "id", 1,
                "name", "Laptop",
                "category", "Electronics",
                "price", 50000.0,
                "quantity", 10
            ),
            Map.of(
                "id", 2,
                "name", "Smartphone",
                "category", "Electronics",
                "price", 25000.0,
                "quantity", 15
            ),
            Map.of(
                "id", 3,
                "name", "Headphones",
                "category", "Electronics",
                "price", 2500.0,
                "quantity", 25
            ),
            Map.of(
                "id", 4,
                "name", "Wireless Mouse",
                "category", "Electronics",
                "price", 1500.0,
                "quantity", 30
            ),
            Map.of(
                "id", 5,
                "name", "USB Cable",
                "category", "Electronics",
                "price", 500.0,
                "quantity", 50
            )
        );
    }
}