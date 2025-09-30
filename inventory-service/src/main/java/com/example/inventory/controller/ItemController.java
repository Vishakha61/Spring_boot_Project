package com.example.inventory.controller;

import com.example.inventory.model.Item;
import com.example.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Inventory API is running");
        response.put("version", "1.0");
        response.put("endpoints", List.of("/api/items"));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        List<Item> items = inventoryService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        try {
            Item item = inventoryService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Item> addItem(@RequestBody Item item) {
        Item savedItem = inventoryService.addItem(item);
        return ResponseEntity.ok(savedItem);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Item> updateStock(@PathVariable Long id, @RequestParam int quantity) {
        try {
            Item updatedItem = inventoryService.reduceStock(id, quantity);
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/stock/add")
    public ResponseEntity<Item> addStock(@PathVariable Long id, @RequestParam int quantity) {
        try {
            Item updatedItem = inventoryService.addStock(id, quantity);
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/stock/check")
    public ResponseEntity<Map<String, Object>> checkStock(@PathVariable Long id, @RequestParam int requiredQuantity) {
        try {
            boolean available = inventoryService.checkStockAvailability(id, requiredQuantity);
            Item item = inventoryService.getItemById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("itemId", id);
            response.put("itemName", item.getName());
            response.put("currentStock", item.getQuantity());
            response.put("requiredQuantity", requiredQuantity);
            response.put("available", available);
            response.put("status", available ? "AVAILABLE" : "INSUFFICIENT_STOCK");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item updatedItem) {
        try {
            Item item = inventoryService.getItemById(id);
            item.setName(updatedItem.getName());
            item.setCategory(updatedItem.getCategory());
            item.setPrice(updatedItem.getPrice());
            item.setQuantity(updatedItem.getQuantity());
            Item savedItem = inventoryService.updateItem(item);
            return ResponseEntity.ok(savedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        try {
            inventoryService.deleteItem(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/sync-to-products")
    public ResponseEntity<Map<String, Object>> syncAllItemsToProducts() {
        try {
            int syncedCount = inventoryService.syncAllItemsToProducts();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Items synced to product service");
            response.put("syncedCount", syncedCount);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failed to sync items: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/{id}/sync-to-product")
    public ResponseEntity<Map<String, Object>> syncItemToProduct(@PathVariable Long id) {
        try {
            boolean synced = inventoryService.syncItemToProduct(id);
            Map<String, Object> response = new HashMap<>();
            if (synced) {
                response.put("message", "Item synced to product service successfully");
                response.put("status", "success");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Item not found");
                response.put("status", "error");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failed to sync item: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }
}
