package com.example.billing.controller;

import com.example.billing.model.Product;
import com.example.billing.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Display all products
    @GetMapping
    public String listProducts(Model model, 
                             @RequestParam(value = "category", required = false) String category,
                             @RequestParam(value = "search", required = false) String search) {
        
        List<Product> products;
        
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProductsByName(search.trim());
            model.addAttribute("searchTerm", search);
        } else if (category != null && !category.trim().isEmpty()) {
            products = productService.getProductsByCategory(category);
            model.addAttribute("selectedCategory", category);
        } else {
            products = productService.getAllActiveProducts();
        }
        
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        
        return "products";
    }

    // Show add product form
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        return "add-product";
    }

    // Handle add product form submission
    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product, 
                           RedirectAttributes redirectAttributes) {
        try {
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Product '" + product.getName() + "' added successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products";
    }

    // Show edit product form
    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model, 
                                    RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.getProductById(id);
        
        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            model.addAttribute("categories", productService.getAllCategories());
            return "edit-product";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Product not found with id: " + id);
            return "redirect:/products";
        }
    }

    // Handle edit product form submission
    @PostMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, 
                            @ModelAttribute Product product,
                            RedirectAttributes redirectAttributes) {
        try {
            productService.updateProduct(id, product);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Product '" + product.getName() + "' updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products";
    }

    // View product details
    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model, 
                            RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.getProductById(id);
        
        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            return "view-product";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Product not found with id: " + id);
            return "redirect:/products";
        }
    }

    // Delete product (soft delete)
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, 
                              RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isPresent()) {
                String productName = productOpt.get().getName();
                productService.deleteProduct(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Product '" + productName + "' deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Product not found with id: " + id);
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products";
    }

    // Stock management page
    @GetMapping("/stock")
    public String stockManagement(Model model) {
        model.addAttribute("products", productService.getAllActiveProducts());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts(5));
        return "stock-management";
    }

    // Update stock
    @PostMapping("/stock/update/{id}")
    public String updateStock(@PathVariable Long id, 
                            @RequestParam Integer newQuantity,
                            RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isPresent()) {
                String productName = productOpt.get().getName();
                productService.updateStock(id, newQuantity);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Stock updated for '" + productName + "' to " + newQuantity + " units!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Product not found with id: " + id);
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products/stock";
    }

    // Add stock
    @PostMapping("/stock/add/{id}")
    public String addStock(@PathVariable Long id, 
                         @RequestParam Integer quantity,
                         RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isPresent()) {
                String productName = productOpt.get().getName();
                productService.addStock(id, quantity);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Added " + quantity + " units to '" + productName + "'!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Product not found with id: " + id);
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products/stock";
    }

    // Initialize sample products (for testing)
    @GetMapping("/init")
    public String initializeSampleProducts(RedirectAttributes redirectAttributes) {
        try {
            productService.initializeSampleProducts();
            redirectAttributes.addFlashAttribute("successMessage", 
                "Sample products initialized successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error initializing sample products: " + e.getMessage());
        }
        return "redirect:/products";
    }

    // REST API endpoints for AJAX calls

    @GetMapping("/api/all")
    @ResponseBody
    public List<Product> getAllProductsAPI() {
        return productService.getAllActiveProducts();
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Product getProductAPI(@PathVariable Long id) {
        return productService.getProductById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @GetMapping("/api/search")
    @ResponseBody
    public List<Product> searchProductsAPI(@RequestParam String query) {
        return productService.searchProductsByName(query);
    }

    @GetMapping("/api/category/{category}")
    @ResponseBody
    public List<Product> getProductsByCategoryAPI(@PathVariable String category) {
        return productService.getProductsByCategory(category);
    }

    @GetMapping("/api/low-stock")
    @ResponseBody
    public List<Product> getLowStockProductsAPI(@RequestParam(defaultValue = "5") Integer threshold) {
        return productService.getLowStockProducts(threshold);
    }
}