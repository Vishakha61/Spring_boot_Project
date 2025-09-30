package com.example.inventory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class RootController {

    @GetMapping("/")
    @ResponseBody
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Inventory Service");
        response.put("status", "Running");
        response.put("version", "1.0");
        response.put("port", 8081);
        response.put("description", "Microservice for managing inventory items");
        response.put("endpoints", Map.of(
                "items", "/api/items",
                "health", "/actuator/health",
                "info", "/actuator/info"
        ));
        return response;
    }

    @GetMapping("/health")
    @ResponseBody
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "inventory-service");
        return response;
    }
}