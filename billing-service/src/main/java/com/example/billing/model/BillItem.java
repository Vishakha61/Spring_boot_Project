package com.example.billing.model;

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

    private int quantity;
    private double price;

    @ManyToOne
    @JoinColumn(name = "bill_id")
    private Bill bill;
}
