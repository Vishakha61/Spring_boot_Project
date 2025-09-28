package com.example.billing.config;

import com.example.billing.model.User;
import com.example.billing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Create a default admin user if it doesn't exist
        if (!userService.existsByUsername("admin")) {
            try {
                User admin = userService.registerUser(
                    "admin", 
                    "admin@example.com", 
                    "admin123", 
                    "Admin", 
                    "User"
                );
                admin.setRole(User.Role.ADMIN);
                System.out.println("Default admin user created: username=admin, password=admin123");
            } catch (Exception e) {
                System.out.println("Could not create default admin user: " + e.getMessage());
            }
        }

        // Create a default regular user if it doesn't exist
        if (!userService.existsByUsername("user")) {
            try {
                userService.registerUser(
                    "user", 
                    "user@example.com", 
                    "user123", 
                    "Demo", 
                    "User"
                );
                System.out.println("Default user created: username=user, password=user123");
            } catch (Exception e) {
                System.out.println("Could not create default user: " + e.getMessage());
            }
        }
    }
}