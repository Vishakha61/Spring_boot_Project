package com.example.inventory.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service", url = "http://localhost:8083")
public interface ProductServiceClient {

    @PostMapping("/api/products")
    ResponseEntity<Object> createProduct(@RequestBody ProductCreateRequest product);
}