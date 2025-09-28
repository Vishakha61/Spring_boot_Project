package com.example.billing.repository;

import com.example.billing.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find products by name (case insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Find products by category
    List<Product> findByCategoryIgnoreCase(String category);
    
    // Find active products only
    List<Product> findByIsActiveTrue();
    
    // Find products with low stock
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.isActive = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    // Find products by category and active status
    List<Product> findByCategoryIgnoreCaseAndIsActiveTrue(String category);
    
    // Find product by SKU
    Optional<Product> findBySku(String sku);
    
    // Check if product name exists (for uniqueness)
    boolean existsByNameIgnoreCase(String name);
    
    // Find products with stock greater than 0
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isActive = true")
    List<Product> findInStockProducts();
    
    // Get all distinct categories
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.isActive = true ORDER BY p.category")
    List<String> findAllCategories();
    
    // Find products by price range
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true")
    List<Product> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                   @Param("maxPrice") java.math.BigDecimal maxPrice);
}