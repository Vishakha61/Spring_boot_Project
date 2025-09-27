package com.example.billing.service;

import com.example.billing.model.Sales;
import com.example.billing.repository.SalesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillingService {

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String inventoryServiceUrl = "http://localhost:8081/api/items";

    public Sales generateBill(Long itemId, int quantity) {
        // Get item from inventory-service
        String url = inventoryServiceUrl + "/" + itemId;
        Map<String, Object> item = restTemplate.getForObject(url, Map.class);
        if (item == null) {
            throw new RuntimeException("Item not found");
        }

        int stock = (int) item.get("quantity");
        if (stock <= 0) {
            throw new RuntimeException("Item is out of stock!");
        }
        if (stock < quantity) {
            throw new RuntimeException("Not enough stock available!");
        }

        double price = ((Number) item.get("price")).doubleValue();
        double totalAmount = price * quantity;

        // Reduce stock via API
        String updateUrl = inventoryServiceUrl + "/" + itemId + "/stock?quantity=" + quantity;
        restTemplate.put(updateUrl, null);

        Sales sale = Sales.builder()
                .itemName((String) item.get("name"))
                .category((String) item.get("category"))
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

    public List<Map<String, Object>> getAllItems() {
        return restTemplate.getForObject(inventoryServiceUrl, List.class);
    }
}
