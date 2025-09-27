package com.example.billing.controller;

import com.example.billing.model.Sales;
import com.example.billing.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class BillingController {

    @Autowired
    private BillingService billingService;

    @GetMapping("/")
    public String home() {
        return "index"; // Thymeleaf template for menu
    }

    @GetMapping("/items")
    public String getItems(Model model) {
        List<Map<String, Object>> items = billingService.getAllItems();
        model.addAttribute("items", items);
        return "items";
    }

    @PostMapping("/add-item")
    public String addItem(@RequestParam String name, @RequestParam String category,
                          @RequestParam double price, @RequestParam int quantity, Model model) {
        // Call inventory-service API to add item
        // Placeholder: model.addAttribute("success", "Item added");
        return "redirect:/items";
    }

    @GetMapping("/generate-bill")
    public String generateBillForm() {
        return "generate-bill";
    }

    @PostMapping("/generate-bill")
    public String generateBill(@RequestParam Long itemId, @RequestParam int quantity, Model model) {
        try {
            Sales sale = billingService.generateBill(itemId, quantity);
            model.addAttribute("sale", sale);
            model.addAttribute("success", "Bill generated successfully!");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "generate-bill";
    }

    @GetMapping("/sales")
    public String getAllSales(Model model) {
        List<Sales> sales = billingService.getAllSales();
        model.addAttribute("sales", sales);
        return "sales";
    }

    @GetMapping("/report")
    public String getSalesReport(Model model) {
        Map<String, Double> report = billingService.getSalesReportByCategory();
        model.addAttribute("report", report);
        return "report";
    }
}
