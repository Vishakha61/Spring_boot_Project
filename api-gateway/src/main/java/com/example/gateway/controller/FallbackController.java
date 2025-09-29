package com.example.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @GetMapping("/inventory-fallback")
    public Mono<String> inventoryFallback() {
        return Mono.just("Inventory service is currently unavailable. Please try again later.");
    }

    @GetMapping("/product-fallback")
    public Mono<String> productFallback() {
        return Mono.just("Product service is currently unavailable. Please try again later.");
    }

    @GetMapping("/billing-fallback")
    public Mono<String> billingFallback() {
        return Mono.just("Billing service is currently unavailable. Please try again later.");
    }
}