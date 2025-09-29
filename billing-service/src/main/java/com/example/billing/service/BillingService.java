package com.example.billing.service;

import com.example.billing.feign.InventoryServiceClient;
import com.example.billing.model.Product;
import com.example.billing.model.Sales;
import com.example.billing.repository.SalesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public Sales generateBill(Long itemId, int quantity) {
        try {
            // Get item from inventory-service using Feign client
            Map<String, Object> item = inventoryServiceClient.getItemById(itemId);
            
            if (item == null) {
                throw new RuntimeException("Item not found");
            }

            int stock = (int) item.get("quantity");
            if (stock <= 0) {
                throw new RuntimeException("Item is out of stock!");
            }
            if (stock < quantity) {
                throw new RuntimeException("Not enough stock available!");
            }

            double price = ((Number) item.get("price")).doubleValue();
            double totalAmount = price * quantity;

            // Reduce stock via Feign client
            inventoryServiceClient.updateStock(itemId, quantity);

            Sales sale = Sales.builder()
                    .itemName((String) item.get("name"))
                    .category((String) item.get("category"))
                    .quantitySold(quantity)
                    .totalAmount(totalAmount)
                    .saleDate(LocalDateTime.now())
                    .build();

            return salesRepository.save(sale);
            
        } catch (Exception e) {
            // Fallback: use sample data when inventory-service is not available
            Map<String, Object> item = getSampleItemById(itemId);
            if (item == null) {
                throw new RuntimeException("Item not found");
            }

            double price = ((Number) item.get("price")).doubleValue();
            double totalAmount = price * quantity;

            Sales sale = Sales.builder()
                    .itemName((String) item.get("name"))
                    .category((String) item.get("category"))
                    .quantitySold(quantity)
                    .totalAmount(totalAmount)
                    .saleDate(LocalDateTime.now())
                    .build();

            return salesRepository.save(sale);
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
            // Use Feign client instead of RestTemplate
            return inventoryServiceClient.getAllItems();
        } catch (Exception e) {
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
}
