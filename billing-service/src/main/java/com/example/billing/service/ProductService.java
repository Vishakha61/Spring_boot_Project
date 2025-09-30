package com.example.billing.service;

import com.example.billing.model.Product;
import com.example.billing.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String PRODUCT_SERVICE_URL = "http://localhost:8083/api/products";

    // Get all active products from Product Service
    public List<Product> getAllActiveProducts() {
        try {
            ResponseEntity<Product[]> response = restTemplate.getForEntity(PRODUCT_SERVICE_URL, Product[].class);
            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error calling Product Service: " + e.getMessage());
            // Fallback to local database
        }
        return productRepository.findByIsActiveTrue();
    }

    // Get all products (including inactive) from Product Service
    public List<Product> getAllProducts() {
        try {
            ResponseEntity<Product[]> response = restTemplate.getForEntity(PRODUCT_SERVICE_URL, Product[].class);
            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error calling Product Service: " + e.getMessage());
            // Fallback to local database
        }
        return productRepository.findAll();
    }

    // Get product by ID from Product Service
    public Optional<Product> getProductById(Long id) {
        try {
            ResponseEntity<Product> response = restTemplate.getForEntity(
                PRODUCT_SERVICE_URL + "/" + id, Product.class);
            if (response.getBody() != null) {
                return Optional.of(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error calling Product Service for ID " + id + ": " + e.getMessage());
            // Fallback to local database
        }
        return productRepository.findById(id);
    }

    // Save or update product via Product Service
    public Product saveProduct(Product product) {
        try {
            // Generate SKU if not provided
            if (product.getSku() == null || product.getSku().trim().isEmpty()) {
                product.setSku(generateSKU(product));
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Product> request = new HttpEntity<>(product, headers);
            
            if (product.getId() != null) {
                // Update existing product
                ResponseEntity<Product> response = restTemplate.exchange(
                    PRODUCT_SERVICE_URL + "/" + product.getId(),
                    HttpMethod.PUT,
                    request,
                    Product.class
                );
                if (response.getBody() != null) {
                    return response.getBody();
                }
            } else {
                // Create new product
                ResponseEntity<Product> response = restTemplate.postForEntity(
                    PRODUCT_SERVICE_URL,
                    request,
                    Product.class
                );
                if (response.getBody() != null) {
                    return response.getBody();
                }
            }
        } catch (Exception e) {
            System.err.println("Error calling Product Service: " + e.getMessage());
            // Fallback to local database
        }
        
        return productRepository.save(product);
    }

    // Create new product
    public Product createProduct(String name, String description, BigDecimal price, 
                               String category, Integer stockQuantity) {
        
        // Check if product with same name already exists
        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Product with name '" + name + "' already exists");
        }
        
        Product product = new Product(name, description, price, category, stockQuantity);
        product.setSku(generateSKU(product));
        
        return productRepository.save(product);
    }

    // Update product via Product Service
    public Product updateProduct(Long id, Product updatedProduct) {
        try {
            updatedProduct.setId(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Product> request = new HttpEntity<>(updatedProduct, headers);
            
            ResponseEntity<Product> response = restTemplate.exchange(
                PRODUCT_SERVICE_URL + "/" + id,
                HttpMethod.PUT,
                request,
                Product.class
            );
            
            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.err.println("Error calling Product Service for update: " + e.getMessage());
            // Fallback to local database
        }
        
        // Fallback to local update
        Optional<Product> existingProductOpt = productRepository.findById(id);
        
        if (existingProductOpt.isPresent()) {
            Product existingProduct = existingProductOpt.get();
            
            // Check if name is being changed and if new name already exists
            if (!existingProduct.getName().equalsIgnoreCase(updatedProduct.getName()) &&
                productRepository.existsByNameIgnoreCase(updatedProduct.getName())) {
                throw new RuntimeException("Product with name '" + updatedProduct.getName() + "' already exists");
            }
            
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setCategory(updatedProduct.getCategory());
            existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
            existingProduct.setIsActive(updatedProduct.getIsActive());
            
            // Update SKU if name or category changed
            if (updatedProduct.getSku() == null || updatedProduct.getSku().trim().isEmpty()) {
                existingProduct.setSku(generateSKU(existingProduct));
            } else {
                existingProduct.setSku(updatedProduct.getSku());
            }
            
            return productRepository.save(existingProduct);
        } else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }

    // Delete product (soft delete - mark as inactive)
    public void deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setIsActive(false);
            productRepository.save(product);
        } else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }

    // Hard delete product
    public void hardDeleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        } else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }

    // Search products by name
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    // Get products by category
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCaseAndIsActiveTrue(category);
    }

    // Get all categories
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    // Get low stock products
    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold != null ? threshold : 5);
    }

    // Get in-stock products
    public List<Product> getInStockProducts() {
        return productRepository.findInStockProducts();
    }

    // Update stock quantity
    public void updateStock(Long productId, Integer newQuantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStockQuantity(newQuantity);
            productRepository.save(product);
        } else {
            throw new RuntimeException("Product not found with id: " + productId);
        }
    }

    // Reduce stock (for sales)
    public void reduceStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStockQuantity() >= quantity) {
                product.reduceStock(quantity);
                productRepository.save(product);
            } else {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
        } else {
            throw new RuntimeException("Product not found with id: " + productId);
        }
    }

    // Add stock (for restocking)
    public void addStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.addStock(quantity);
            productRepository.save(product);
        } else {
            throw new RuntimeException("Product not found with id: " + productId);
        }
    }

    // Find products by price range
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    // Generate SKU based on category and name
    private String generateSKU(Product product) {
        String categoryCode = product.getCategory().toUpperCase().substring(0, 
            Math.min(3, product.getCategory().length()));
        String nameCode = product.getName().toUpperCase().replaceAll("[^A-Z0-9]", "")
            .substring(0, Math.min(4, product.getName().replaceAll("[^A-Za-z0-9]", "").length()));
        
        // Add timestamp to ensure uniqueness
        long timestamp = System.currentTimeMillis() % 10000;
        
        return categoryCode + "-" + nameCode + "-" + timestamp;
    }

    // Initialize sample products
    public void initializeSampleProducts() {
        if (productRepository.count() == 0) {
            List<Product> sampleProducts = List.of(
                new Product("Dell Laptop", "High-performance laptop for professionals", 
                    new BigDecimal("85000.00"), "Electronics", 15),
                new Product("iPhone 15", "Latest Apple smartphone with advanced features", 
                    new BigDecimal("79999.00"), "Electronics", 20),
                new Product("Sony Headphones", "Noise-cancelling wireless headphones", 
                    new BigDecimal("15999.00"), "Electronics", 30),
                new Product("Logitech Mouse", "Wireless gaming mouse with precision tracking", 
                    new BigDecimal("2499.00"), "Electronics", 50),
                new Product("Samsung Monitor", "27-inch 4K display monitor", 
                    new BigDecimal("28999.00"), "Electronics", 12),
                new Product("Office Chair", "Ergonomic office chair with lumbar support", 
                    new BigDecimal("12999.00"), "Furniture", 8),
                new Product("Standing Desk", "Adjustable height standing desk", 
                    new BigDecimal("25999.00"), "Furniture", 5),
                new Product("Coffee Maker", "Automatic drip coffee maker", 
                    new BigDecimal("4999.00"), "Appliances", 25),
                new Product("Water Bottle", "Stainless steel insulated water bottle", 
                    new BigDecimal("999.00"), "Accessories", 100)
            );

            for (Product product : sampleProducts) {
                product.setSku(generateSKU(product));
                productRepository.save(product);
            }
        }
    }
}