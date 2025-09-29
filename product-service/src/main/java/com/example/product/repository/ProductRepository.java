package com.example.product.repository;

import com.example.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByIsActiveTrue();
    
    List<Product> findByCategoryAndIsActiveTrue(String category);
    
    Optional<Product> findBySkuAndIsActiveTrue(String sku);
    
    Optional<Product> findByNameAndIsActiveTrue(String name);
    
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.isActive = true")
    List<String> findDistinctCategories();
    
    List<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity < :threshold")
    List<Product> findLowStockProducts(int threshold);
}