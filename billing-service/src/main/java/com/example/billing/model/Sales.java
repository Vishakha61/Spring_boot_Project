package com.example.billing.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String category;
    private int quantitySold;
    private double totalAmount;
    private LocalDateTime saleDate;

    // Explicit getters and setters to ensure they're available
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    // Builder method
    public static SalesBuilder builder() {
        return new SalesBuilder();
    }

    public static class SalesBuilder {
        private Long id;
        private String itemName;
        private String category;
        private int quantitySold;
        private double totalAmount;
        private LocalDateTime saleDate;

        public SalesBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SalesBuilder itemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        public SalesBuilder category(String category) {
            this.category = category;
            return this;
        }

        public SalesBuilder quantitySold(int quantitySold) {
            this.quantitySold = quantitySold;
            return this;
        }

        public SalesBuilder totalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public SalesBuilder saleDate(LocalDateTime saleDate) {
            this.saleDate = saleDate;
            return this;
        }

        public Sales build() {
            Sales sales = new Sales();
            sales.id = this.id;
            sales.itemName = this.itemName;
            sales.category = this.category;
            sales.quantitySold = this.quantitySold;
            sales.totalAmount = this.totalAmount;
            sales.saleDate = this.saleDate;
            return sales;
        }
    }
}
