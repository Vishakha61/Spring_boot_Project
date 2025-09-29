package com.example.billing;

import com.example.billing.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class BillingApplication implements CommandLineRunner {

    @Autowired
    private ProductService productService;

    public static void main(String[] args) {
        SpringApplication.run(BillingApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize sample products if database is empty
        productService.initializeSampleProducts();
        System.out.println("Sample products initialized (if database was empty)");
    }
}
