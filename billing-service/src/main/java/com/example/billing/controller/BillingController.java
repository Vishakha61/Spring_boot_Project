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

    @GetMapping("/inventory")
    public String inventory() {
        return "inventory-dashboard"; // Inventory section landing page
    }

    @GetMapping("/billing")
    public String billing() {
        return "billing-dashboard"; // Billing section landing page
    }

    @GetMapping("/items")
    public String getItems(Model model) {
        List<Map<String, Object>> items = billingService.getAllItems();
        model.addAttribute("items", items);
        return "items";
    }

    @GetMapping("/add-item")
    public String addItemForm() {
        return "add-item";
    }

    @PostMapping("/add-item")
    public String addItem(@RequestParam String name, @RequestParam String category,
                          @RequestParam double price, @RequestParam int quantity, Model model) {
        try {
            billingService.addItem(name, category, price, quantity);
            model.addAttribute("success", "Item added successfully!");
            return "redirect:/items";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add item: " + e.getMessage());
            return "add-item";
        }
    }

    @GetMapping("/edit-item/{id}")
    public String editItemForm(@PathVariable Long id, Model model) {
        try {
            Map<String, Object> item = billingService.getItemById(id);
            model.addAttribute("item", item);
            return "edit-item";
        } catch (Exception e) {
            model.addAttribute("error", "Item not found");
            return "redirect:/items";
        }
    }

    @PostMapping("/edit-item/{id}")
    public String updateItem(@PathVariable Long id, @RequestParam String name, 
                           @RequestParam String category, @RequestParam double price, 
                           @RequestParam int quantity, Model model) {
        try {
            billingService.updateItem(id, name, category, price, quantity);
            model.addAttribute("success", "Item updated successfully!");
            return "redirect:/items";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update item: " + e.getMessage());
            return "redirect:/edit-item/" + id;
        }
    }

    @PostMapping("/delete-item/{id}")
    public String deleteItem(@PathVariable Long id, Model model) {
        try {
            billingService.deleteItem(id);
            model.addAttribute("success", "Item deleted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete item: " + e.getMessage());
        }
        return "redirect:/items";
    }

    @GetMapping("/generate-bill")
    public String generateBillForm(
            @RequestParam(value = "bulk", required = false) String bulk,
            @RequestParam(value = "quick", required = false) String quick,
            Model model) {
        
        List<Map<String, Object>> items = billingService.getAllItems();
        model.addAttribute("items", items);
        
        // Set page attributes based on parameters
        if ("true".equals(bulk)) {
            model.addAttribute("pageTitle", "Bulk Billing");
            model.addAttribute("pageDescription", "Generate multiple bills in batch processing mode");
            model.addAttribute("bulkMode", true);
        } else if ("true".equals(quick)) {
            model.addAttribute("pageTitle", "Quick Bill");
            model.addAttribute("pageDescription", "Fast checkout for walk-in customers");
            model.addAttribute("quickMode", true);
        } else {
            model.addAttribute("pageTitle", "Generate New Bill");
            model.addAttribute("pageDescription", "Create professional bills for customers");
        }
        
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

    @GetMapping("/report")
    public String getSalesReport(
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "dashboard", required = false) String dashboard,
            Model model) {
        
        Map<String, Double> report = billingService.getSalesReportByCategory();
        model.addAttribute("report", report);
        
        // Add period-specific data
        if ("week".equals(period)) {
            model.addAttribute("reportTitle", "Weekly Sales Report");
            model.addAttribute("reportPeriod", "This Week");
        } else if ("month".equals(period)) {
            model.addAttribute("reportTitle", "Monthly Sales Report");
            model.addAttribute("reportPeriod", "This Month");
        } else if ("true".equals(dashboard)) {
            model.addAttribute("reportTitle", "Analytics Dashboard");
            model.addAttribute("showCharts", true);
        } else {
            model.addAttribute("reportTitle", "Sales Report");
        }
        
        return "report";
    }
    
    @GetMapping("/sales")
    public String getAllSales(
            @RequestParam(value = "today", required = false) String today,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "export", required = false) String export,
            @RequestParam(value = "recent", required = false) String recent,
            Model model) {
        
        List<Sales> sales = billingService.getAllSales();
        
        // Filter sales based on parameters
        if ("true".equals(today)) {
            model.addAttribute("pageTitle", "Today's Sales");
            model.addAttribute("showDateFilter", true);
        } else if ("true".equals(search)) {
            model.addAttribute("pageTitle", "Search Bills & Sales");
            model.addAttribute("showSearchForm", true);
        } else if ("true".equals(export)) {
            model.addAttribute("pageTitle", "Export Sales Data");
            model.addAttribute("showExportOptions", true);
        } else if ("true".equals(recent)) {
            model.addAttribute("pageTitle", "Recent Transactions");
            // Limit to last 10 transactions
            if (sales.size() > 10) {
                sales = sales.subList(0, 10);
            }
        } else {
            model.addAttribute("pageTitle", "All Sales");
        }
        
        // Calculate statistics
        double totalRevenue = sales.stream().mapToDouble(Sales::getTotalAmount).sum();
        int totalItemsSold = sales.stream().mapToInt(Sales::getQuantitySold).sum();
        
        model.addAttribute("sales", sales);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalItemsSold", totalItemsSold);
        
        return "sales";
    }
    
    @GetMapping("/settings")
    public String systemSettings(Model model) {
        model.addAttribute("pageTitle", "System Settings");
        model.addAttribute("settings", Map.of(
            "currency", "INR",
            "taxRate", "18.0",
            "companyName", "Billing System",
            "lowStockAlert", "5"
        ));
        return "settings";
    }
}
