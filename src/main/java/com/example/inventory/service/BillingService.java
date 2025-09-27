package com.example.inventory.service;

import com.example.inventory.model.Item;
import com.example.inventory.model.Sales;
import com.example.inventory.repository.ItemRepository;
import com.example.inventory.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private final ItemRepository itemRepository;
    private final SalesRepository salesRepository;

    public BillingService(ItemRepository itemRepository, SalesRepository salesRepository) {
        this.itemRepository = itemRepository;
        this.salesRepository = salesRepository;
    }

    public Sales generateBill(Long itemId, int quantity) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getQuantity() <= 0) {
            throw new RuntimeException("Item is out of stock!");
        }
        if (item.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available!");
        }

        double totalAmount = item.getPrice() * quantity;

        // reduce stock
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        Sales sale = Sales.builder()
                .itemName(item.getName())
                .category(item.getCategory())
                .quantitySold(quantity)
                .totalAmount(totalAmount)
                .saleDate(LocalDateTime.now())
                .build();

        return salesRepository.save(sale);
    }

    public List<Sales> getAllSales() {
        return salesRepository.findAll();
    }

    public Map<String, Double> getSalesReportByCategory() {
        return salesRepository.findAll().stream()
                .collect(Collectors.groupingBy(Sales::getCategory,
                        Collectors.summingDouble(Sales::getTotalAmount)));
    }
}
