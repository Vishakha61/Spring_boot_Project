package com.example.billing.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int billId;

    private double total;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BillItem> items = new ArrayList<>();
}
