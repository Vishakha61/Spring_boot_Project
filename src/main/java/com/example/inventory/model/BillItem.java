package com.example.inventory.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private int quantity;
    private double price;

    @ManyToOne
    @JoinColumn(name = "bill_id")
    private Bill bill;   // FIX: so we can call setBill()
}
