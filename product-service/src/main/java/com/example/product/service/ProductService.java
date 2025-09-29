package com.example.product.service;

import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductByName(String name) {
        return productRepository.findByNameAndIsActiveTrue(name);
    }

    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySkuAndIsActiveTrue(sku);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndIsActiveTrue(category);
    }

    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    public Product createProduct(Product product) {
        // Check if product with same name already exists
        Optional<Product> existingProduct = productRepository.findByNameAndIsActiveTrue(product.getName());
        if (existingProduct.isPresent()) {
            throw new RuntimeException("Product with name '" + product.getName() + "' already exists");
        }

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setIsActive(true);

        // Generate SKU if not provided
        if (product.getSku() == null || product.getSku().isEmpty()) {
            product.setSku(generateSku(product.getName()));
        }

        return productRepository.save(product);
    }

    public Product updateProduct(Product product) {
        Optional<Product> existingProductOpt = productRepository.findById(product.getId());
        if (existingProductOpt.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + product.getId());
        }

        Product existingProduct = existingProductOpt.get();
        existingProduct.setName(product.getName());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQuantity(product.getStockQuantity());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Soft delete - mark as inactive instead of physically deleting
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    public void hardDeleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public Product deactivateProduct(Long id) {
        Product product = getProductById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }

    public Product updateStock(Long id, Integer newStock) {
        Product product = getProductById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setStockQuantity(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }

    public Product reduceStock(Long id, Integer quantity) {
        Product product = getProductById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity() + ", Required: " + quantity);
        }
        
        product.setStockQuantity(product.getStockQuantity() - quantity);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }

    public Product addStock(Long id, Integer quantity) {
        Product product = getProductById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setStockQuantity(product.getStockQuantity() + quantity);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }

    public Product increaseStock(Long id, Integer quantity) {
        return addStock(id, quantity);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query);
    }

    public long getTotalProducts() {
        return productRepository.count();
    }

    public long getTotalProductCount() {
        return productRepository.count();
    }

    public long getActiveProductCount() {
        return productRepository.findByIsActiveTrue().size();
    }

    public long getActiveProductsCount() {
        return productRepository.findByIsActiveTrue().size();
    }

    public BigDecimal getTotalInventoryValue() {
        return productRepository.findByIsActiveTrue().stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateSku(String productName) {
        // Simple SKU generation: first 3 letters of product name + timestamp
        String prefix = productName.replaceAll("\\s+", "").substring(0, Math.min(3, productName.length())).toUpperCase();
        long timestamp = System.currentTimeMillis() % 10000;
        return prefix + timestamp;
    }
}