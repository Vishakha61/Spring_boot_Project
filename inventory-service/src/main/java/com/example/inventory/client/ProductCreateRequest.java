package com.example.inventory.client;

import java.math.BigDecimal;

public class ProductCreateRequest {
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String sku;
    private Boolean isActive = true;

    // Constructors
    public ProductCreateRequest() {}

    public ProductCreateRequest(String name, String category, String description, 
                               BigDecimal price, Integer stockQuantity, String sku, Boolean isActive) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.sku = sku;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}