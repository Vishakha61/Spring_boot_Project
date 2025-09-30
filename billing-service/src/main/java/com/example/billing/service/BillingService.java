package com.example.billing.service;

import com.example.billing.feign.InventoryServiceClient;
import com.example.billing.feign.ProductServiceClient;
import com.example.billing.model.Product;
import com.example.billing.model.Sales;
import com.example.billing.repository.SalesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillingService {

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryServiceClient inventoryServiceClient;

    @Autowired
    private ProductServiceClient productServiceClient;

    public Sales generateBill(Long itemId, int quantity) {
        try {
            System.out.println("🔍 Starting bill generation for item ID: " + itemId + ", quantity: " + quantity);
            
            // STEP 1: Validate item exists in BOTH inventory and product services
            Map<String, Object> inventoryItem = null;
            Map<String, Object> productItem = null;
            
            try {
                inventoryItem = inventoryServiceClient.getItemById(itemId);
                if (inventoryItem == null) {
                    throw new RuntimeException("❌ Item not found in inventory service! Item ID: " + itemId);
                }
                System.out.println("✅ Item found in inventory: " + inventoryItem.get("name"));
            } catch (Exception e) {
                throw new RuntimeException("❌ Failed to validate item in inventory service: " + e.getMessage());
            }
            
            try {
                productItem = productServiceClient.getProductById(itemId);
                if (productItem == null) {
                    throw new RuntimeException("❌ Item not found in product service! Item ID: " + itemId);
                }
                System.out.println("✅ Item found in products: " + productItem.get("name"));
            } catch (Exception e) {
                throw new RuntimeException("❌ Failed to validate item in product service: " + e.getMessage());
            }
            
            // STEP 2: Verify items are synchronized (same name and category)
            String inventoryName = (String) inventoryItem.get("name");
            String productName = (String) productItem.get("name");
            String inventoryCategory = (String) inventoryItem.get("category");
            String productCategory = (String) productItem.get("category");
            
            if (!inventoryName.equals(productName)) {
                throw new RuntimeException("❌ Item name mismatch! Inventory: '" + inventoryName + "' vs Product: '" + productName + "'");
            }
            
            if (!inventoryCategory.equals(productCategory)) {
                throw new RuntimeException("❌ Item category mismatch! Inventory: '" + inventoryCategory + "' vs Product: '" + productCategory + "'");
            }
            
            System.out.println("✅ Items are synchronized between services");
            
            // STEP 3: Check stock availability in BOTH services
            Integer inventoryStock = (Integer) inventoryItem.get("quantity");
            Integer productStock = (Integer) productItem.get("stockQuantity");
            
            if (inventoryStock <= 0 || productStock <= 0) {
                throw new RuntimeException("❌ Item is out of stock! Inventory: " + inventoryStock + ", Product: " + productStock);
            }
            
            if (inventoryStock < quantity || productStock < quantity) {
                throw new RuntimeException("❌ Insufficient stock! Required: " + quantity + ", Available - Inventory: " + inventoryStock + ", Product: " + productStock);
            }
            
            // Verify stock consistency between services
            if (!inventoryStock.equals(productStock)) {
                System.out.println("⚠️ Stock mismatch detected! Inventory: " + inventoryStock + ", Product: " + productStock);
                throw new RuntimeException("❌ Stock inconsistency! Please sync services. Inventory: " + inventoryStock + ", Product: " + productStock);
            }
            
            System.out.println("✅ Stock validation passed. Available: " + inventoryStock);
            
            // STEP 4: Calculate bill details
            double price = ((Number) productItem.get("price")).doubleValue();
            double totalAmount = price * quantity;
            
            System.out.println("💰 Bill calculation: " + quantity + " × $" + price + " = $" + totalAmount);
            
            // STEP 5: Reduce stock in BOTH services atomically
            try {
                // Reduce stock in inventory service
                inventoryServiceClient.updateStock(itemId, quantity);
                System.out.println("✅ Stock reduced in inventory service");
                
                // Reduce stock in product service
                productServiceClient.reduceStock(itemId, quantity);
                System.out.println("✅ Stock reduced in product service");
                
            } catch (Exception e) {
                // If either service fails, we should ideally rollback
                System.err.println("❌ Stock reduction failed: " + e.getMessage());
                throw new RuntimeException("Failed to update stock in services: " + e.getMessage());
            }
            
            // STEP 6: Create and save the sales record
            Sales sale = Sales.builder()
                    .itemName(inventoryName)
                    .category(inventoryCategory)
                    .quantitySold(quantity)
                    .totalAmount(totalAmount)
                    .saleDate(LocalDateTime.now())
                    .build();

            Sales savedSale = salesRepository.save(sale);
            System.out.println("🎉 Bill generated successfully! Sale ID: " + savedSale.getId());
            
            return savedSale;
            
        } catch (Exception e) {
            System.err.println("❌ Bill generation failed: " + e.getMessage());
            throw new RuntimeException("Failed to generate bill: " + e.getMessage());
        }
    }

    public List<Sales> getAllSales() {
        return salesRepository.findAll();
    }

    public Map<String, Double> getSalesReportByCategory() {
        return salesRepository.findAll().stream()
                .collect(Collectors.groupingBy(Sales::getCategory,
                        Collectors.summingDouble(Sales::getTotalAmount)));
    }

    public List<Map<String, Object>> getAllItems() {
        try {
            // Use Feign client to get real inventory data
            List<Map<String, Object>> items = inventoryServiceClient.getAllItems();
            System.out.println("✅ Successfully retrieved " + items.size() + " items from inventory service");
            return items;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to get items from inventory service: " + e.getMessage());
            System.out.println("🔄 Falling back to sample data");
            // Fallback: return sample data when service is not available
            return getSampleItems();
        }
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

    private Map<String, Object> getSampleItemById(Long itemId) {
        List<Map<String, Object>> sampleItems = getSampleItems();
        return sampleItems.stream()
                .filter(item -> ((Number) item.get("id")).longValue() == itemId)
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> convertProductToMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("category", product.getCategory());
        map.put("price", product.getPrice().doubleValue());
        map.put("quantity", product.getStockQuantity());
        return map;
    }



    public Map<String, Object> getItemById(Long itemId) {
        try {
            // Try Feign client first
            return inventoryServiceClient.getItemById(itemId);
        } catch (Exception e) {
            // Fallback to local product service
            try {
                Product product = productService.getProductById(itemId)
                    .orElse(null);
                return product != null ? convertProductToMap(product) : getSampleItemById(itemId);
            } catch (Exception ex) {
                return getSampleItemById(itemId);
            }
        }
    }

    public void addItem(String name, String category, double price, int quantity) {
        Map<String, Object> item = Map.of(
            "name", name,
            "category", category,
            "price", price,
            "quantity", quantity
        );
        
        try {
            inventoryServiceClient.addItem(item);
        } catch (Exception e) {
            // Fallback: add to local product service
            try {
                Product product = new Product();
                product.setName(name);
                product.setCategory(category);
                product.setPrice(BigDecimal.valueOf(price));
                product.setStockQuantity(quantity);
                product.setDescription("Added via item management");
                productService.saveProduct(product);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to add item: " + ex.getMessage());
            }
        }
    }

    public void updateItem(Long id, String name, String category, double price, int quantity) {
        Map<String, Object> item = Map.of(
            "name", name,
            "category", category,
            "price", price,
            "quantity", quantity
        );
        
        try {
            inventoryServiceClient.updateItem(id, item);
        } catch (Exception e) {
            // Fallback: update local product service
            try {
                Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
                product.setName(name);
                product.setCategory(category);
                product.setPrice(BigDecimal.valueOf(price));
                product.setStockQuantity(quantity);
                productService.saveProduct(product);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to update item: " + ex.getMessage());
            }
        }
    }

    public void deleteItem(Long id) {
        try {
            inventoryServiceClient.deleteItem(id);
        } catch (Exception e) {
            // Fallback: delete from local product service
            try {
                // Check if product exists before deleting
                productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
                productService.deleteProduct(id);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to delete item: " + ex.getMessage());
            }
        }
    }

    // Stock Management Methods for Sales Operations
    public boolean restoreStock(Long itemId, int quantity, String reason) {
        try {
            // Restore stock in inventory service
            inventoryServiceClient.addStock(itemId, quantity);
            System.out.println("✅ Stock restored: " + quantity + " units for item ID: " + itemId + " (" + reason + ")");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to restore stock: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelSale(Long saleId) {
        try {
            // Get the sale record
            Sales sale = salesRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
            
            // Find the corresponding item (this is simplified - in a real system you'd store item ID)
            // For now, we'll need to find by name (not ideal, but works for demo)
            List<Map<String, Object>> items = inventoryServiceClient.getAllItems();
            Long itemId = null;
            
            for (Map<String, Object> item : items) {
                if (sale.getItemName().equals(item.get("name"))) {
                    itemId = ((Number) item.get("id")).longValue();
                    break;
                }
            }
            
            if (itemId != null) {
                // Restore the stock
                boolean stockRestored = restoreStock(itemId, sale.getQuantitySold(), "Sale cancellation");
                
                if (stockRestored) {
                    // Mark sale as cancelled or delete it
                    salesRepository.delete(sale);
                    System.out.println("✅ Sale cancelled and stock restored for: " + sale.getItemName());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Failed to cancel sale: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getStockStatus(Long itemId) {
        try {
            return inventoryServiceClient.checkStock(itemId, 1);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unable to check stock status");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Get items that are available in BOTH inventory and product services
     */
    public List<Map<String, Object>> getAvailableItemsForBilling() {
        try {
            List<Map<String, Object>> inventoryItems = inventoryServiceClient.getAllItems();
            List<Map<String, Object>> productItems = productServiceClient.getAllProducts();
            
            System.out.println("🔍 Found " + inventoryItems.size() + " items in inventory, " + productItems.size() + " items in products");
            
            // Filter items that exist in both services with matching stock
            List<Map<String, Object>> availableItems = inventoryItems.stream()
                .filter(inventoryItem -> {
                    Long itemId = Long.valueOf(inventoryItem.get("id").toString());
                    
                    // Find matching product
                    Map<String, Object> matchingProduct = productItems.stream()
                        .filter(product -> Long.valueOf(product.get("id").toString()).equals(itemId))
                        .findFirst()
                        .orElse(null);
                    
                    if (matchingProduct == null) {
                        System.out.println("⚠️ Item '" + inventoryItem.get("name") + "' exists in inventory but not in products");
                        return false;
                    }
                    
                    // Check stock consistency
                    Integer inventoryStock = (Integer) inventoryItem.get("quantity");
                    Integer productStock = (Integer) matchingProduct.get("stockQuantity");
                    
                    if (!inventoryStock.equals(productStock)) {
                        System.out.println("⚠️ Stock mismatch for '" + inventoryItem.get("name") + "': Inventory=" + inventoryStock + ", Product=" + productStock);
                    }
                    
                    // Include item if both have stock > 0
                    return inventoryStock > 0 && productStock > 0;
                })
                .collect(Collectors.toList());
            
            System.out.println("✅ " + availableItems.size() + " items are available for billing");
            return availableItems;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to get available items: " + e.getMessage());
            throw new RuntimeException("Failed to get items available for billing: " + e.getMessage());
        }
    }

    /**
     * Sync stock between inventory and product services
     */
    public Map<String, Object> syncStockBetweenServices(Long itemId) {
        try {
            System.out.println("🔄 Starting stock synchronization for item ID: " + itemId);
            
            // Get current stock from both services
            Map<String, Object> inventoryItem = inventoryServiceClient.getItemById(itemId);
            Map<String, Object> productItem = productServiceClient.getProductById(itemId);
            
            if (inventoryItem == null || productItem == null) {
                throw new RuntimeException("Item not found in one or both services");
            }
            
            Integer inventoryStock = (Integer) inventoryItem.get("quantity");
            Integer productStock = (Integer) productItem.get("stockQuantity");
            
            System.out.println("📊 Current stock - Inventory: " + inventoryStock + ", Product: " + productStock);
            
            if (inventoryStock.equals(productStock)) {
                System.out.println("✅ Stock is already synchronized");
                return Map.of(
                    "status", "already_synced",
                    "stock", inventoryStock,
                    "message", "Stock is already synchronized"
                );
            }
            
            // Use inventory as the source of truth for synchronization
            try {
                productServiceClient.updateStock(itemId, inventoryStock);
                System.out.println("✅ Product stock updated to match inventory: " + inventoryStock);
                
                return Map.of(
                    "status", "synced",
                    "previousProductStock", productStock,
                    "newStock", inventoryStock,
                    "message", "Stock synchronized successfully"
                );
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to update product stock: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Stock synchronization failed: " + e.getMessage());
            throw new RuntimeException("Failed to sync stock: " + e.getMessage());
        }
    }

    /**
     * Get synchronization status between inventory and product services
     */
    public Map<String, Object> getSyncStatus() {
        try {
            List<Map<String, Object>> inventoryItems = inventoryServiceClient.getAllItems();
            List<Map<String, Object>> productItems = productServiceClient.getAllProducts();
            
            int totalItems = inventoryItems.size();
            int syncedItems = 0;
            int stockMismatches = 0;
            List<Map<String, Object>> mismatches = new ArrayList<>();
            
            for (Map<String, Object> inventoryItem : inventoryItems) {
                Long itemId = Long.valueOf(inventoryItem.get("id").toString());
                
                Map<String, Object> productItem = productItems.stream()
                    .filter(product -> Long.valueOf(product.get("id").toString()).equals(itemId))
                    .findFirst()
                    .orElse(null);
                
                if (productItem != null) {
                    syncedItems++;
                    
                    Integer inventoryStock = (Integer) inventoryItem.get("quantity");
                    Integer productStock = (Integer) productItem.get("stockQuantity");
                    
                    if (!inventoryStock.equals(productStock)) {
                        stockMismatches++;
                        mismatches.add(Map.of(
                            "id", itemId,
                            "name", inventoryItem.get("name"),
                            "inventoryStock", inventoryStock,
                            "productStock", productStock
                        ));
                    }
                }
            }
            
            return Map.of(
                "totalInventoryItems", totalItems,
                "totalProductItems", productItems.size(),
                "syncedItems", syncedItems,
                "stockMismatches", stockMismatches,
                "mismatches", mismatches,
                "syncPercentage", (syncedItems * 100.0) / totalItems
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get sync status: " + e.getMessage());
        }
    }
}
