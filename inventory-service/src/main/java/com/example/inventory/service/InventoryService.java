package com.example.inventory.service;

import com.example.inventory.client.ProductCreateRequest;
import com.example.inventory.client.ProductServiceClient;
import com.example.inventory.model.Item;
import com.example.inventory.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final ItemRepository itemRepository;
    private final ProductServiceClient productServiceClient;

    public InventoryService(ItemRepository itemRepository, ProductServiceClient productServiceClient) {
        this.itemRepository = itemRepository;
        this.productServiceClient = productServiceClient;
    }

    public Item addItem(Item item) {
        // Save item to inventory
        Item savedItem = itemRepository.save(item);
        logger.info("Item added to inventory: {}", savedItem.getName());
        
        // Sync with product service
        try {
            ProductCreateRequest productRequest = new ProductCreateRequest();
            productRequest.setName(savedItem.getName());
            productRequest.setCategory(savedItem.getCategory());
            productRequest.setDescription("Added from inventory - " + savedItem.getName());
            productRequest.setPrice(BigDecimal.valueOf(savedItem.getPrice()));
            productRequest.setStockQuantity(savedItem.getQuantity());
            productRequest.setSku("INV-" + savedItem.getId());
            productRequest.setIsActive(true);
                    
            productServiceClient.createProduct(productRequest);
            logger.info("✅ Item synced to product service: {}", savedItem.getName());
        } catch (Exception e) {
            logger.warn("⚠️ Failed to sync item to product service: {}", e.getMessage());
            // Continue - inventory item is saved even if product sync fails
        }
        
        return savedItem;
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    public Item updateItem(Item item) {
        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new RuntimeException("Item not found with id: " + id);
        }
        itemRepository.deleteById(id);
    }

    public int syncAllItemsToProducts() {
        List<Item> allItems = itemRepository.findAll();
        int syncedCount = 0;
        
        for (Item item : allItems) {
            try {
                syncSingleItemToProduct(item);
                syncedCount++;
                logger.info("✅ Synced item {} to product service", item.getName());
            } catch (Exception e) {
                logger.warn("⚠️ Failed to sync item {} to product service: {}", item.getName(), e.getMessage());
            }
        }
        
        logger.info("Sync completed: {}/{} items synced to product service", syncedCount, allItems.size());
        return syncedCount;
    }

    public boolean syncItemToProduct(Long itemId) {
        try {
            Item item = getItemById(itemId);
            syncSingleItemToProduct(item);
            logger.info("✅ Item {} synced to product service", item.getName());
            return true;
        } catch (RuntimeException e) {
            logger.warn("⚠️ Item not found with id: {}", itemId);
            return false;
        } catch (Exception e) {
            logger.warn("⚠️ Failed to sync item with id {} to product service: {}", itemId, e.getMessage());
            throw e;
        }
    }

    private void syncSingleItemToProduct(Item item) {
        ProductCreateRequest productRequest = new ProductCreateRequest();
        productRequest.setName(item.getName());
        productRequest.setCategory(item.getCategory());
        productRequest.setDescription("Synced from inventory - " + item.getName());
        productRequest.setPrice(BigDecimal.valueOf(item.getPrice()));
        productRequest.setStockQuantity(item.getQuantity());
        productRequest.setSku("INV-" + item.getId());
        productRequest.setIsActive(true);
                
        productServiceClient.createProduct(productRequest);
    }

    // Stock Management Methods
    public Item reduceStock(Long itemId, int quantity) {
        Item item = getItemById(itemId);
        
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }
        
        if (item.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + item.getQuantity() + ", Required: " + quantity);
        }
        
        item.setQuantity(item.getQuantity() - quantity);
        Item updatedItem = updateItem(item);
        
        logger.info("📉 Stock reduced for item {}: {} units (remaining: {})", 
                   item.getName(), quantity, updatedItem.getQuantity());
        
        // Sync stock change to product service
        try {
            syncStockToProductService(updatedItem);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to sync stock reduction to product service: {}", e.getMessage());
        }
        
        return updatedItem;
    }

    public Item addStock(Long itemId, int quantity) {
        Item item = getItemById(itemId);
        
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }
        
        item.setQuantity(item.getQuantity() + quantity);
        Item updatedItem = updateItem(item);
        
        logger.info("📈 Stock added for item {}: {} units (total: {})", 
                   item.getName(), quantity, updatedItem.getQuantity());
        
        // Sync stock change to product service
        try {
            syncStockToProductService(updatedItem);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to sync stock addition to product service: {}", e.getMessage());
        }
        
        return updatedItem;
    }

    public boolean checkStockAvailability(Long itemId, int requiredQuantity) {
        try {
            Item item = getItemById(itemId);
            return item.getQuantity() >= requiredQuantity;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void syncStockToProductService(Item item) {
        // This would require a separate endpoint in product service to update stock
        // For now, we'll log the intention
        logger.info("🔄 Would sync stock update to product service for item: {} (stock: {})", 
                   item.getName(), item.getQuantity());
    }
}
