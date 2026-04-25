package com.example.ecommerce;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // Only add sample products if database is empty
        if (productRepository.count() == 0) {
            List<Product> products = List.of(
                    new Product(null, "Fresh Apples", "Crisp and juicy red apples from local farms", 150.00, "Fruits",
                            50),
                    new Product(null, "Milk", "Fresh whole milk, 1 liter pack", 60.00, "Dairy", 30),
                    new Product(null, "Whole Wheat Bread", "Freshly baked whole wheat bread", 45.00, "Bakery", 25),
                    new Product(null, "Farm Eggs", "Farm fresh eggs, pack of 12", 80.00, "Dairy", 40),
                    new Product(null, "Chicken Breast", "Boneless chicken breast, 1kg", 450.00, "Meat", 20),
                    new Product(null, "Basmati Rice", "Premium quality basmati rice, 5kg", 350.00, "Grains", 15),
                    new Product(null, "Fresh Tomatoes", "Fresh red tomatoes, 1kg", 40.00, "Vegetables", 35),
                    new Product(null, "Potatoes", "Fresh potatoes, 5kg bag", 120.00, "Vegetables", 25),
                    new Product(null, "Onions", "Fresh yellow onions, 1kg", 35.00, "Vegetables", 40),
                    new Product(null, "Carrots", "Fresh organic carrots, 1kg", 50.00, "Vegetables", 30),
                    new Product(null, "Greek Yogurt", "Creamy greek yogurt, 500g", 90.00, "Dairy", 20),
                    new Product(null, "Butter", "Pure butter, 500g pack", 220.00, "Dairy", 15),
                    new Product(null, "Cheddar Cheese", "Aged cheddar cheese, 400g", 350.00, "Dairy", 18),
                    new Product(null, "Orange Juice", "Fresh orange juice, 1 liter", 150.00, "Beverages", 22),
                    new Product(null, "Green Grapes", "Sweet green grapes, 1kg", 180.00, "Fruits", 28),
                    new Product(null, "Strawberries", "Fresh strawberries, 500g", 200.00, "Fruits", 25),
                    new Product(null, "Broccoli", "Fresh broccoli, 1kg", 80.00, "Vegetables", 30),
                    new Product(null, "Spinach", "Organic spinach leaves, 500g", 45.00, "Vegetables", 35),
                    new Product(null, "Cucumber", "Fresh cucumber, 1kg", 30.00, "Vegetables", 40),
                    new Product(null, "Corn Flakes", "Kellogg's corn flakes, 375g", 250.00, "Breakfast", 20),
                    new Product(null, "Oatmeal", "Quaker oats, 1kg", 280.00, "Breakfast", 18),
                    new Product(null, "Pasta", "Barilla penne pasta, 500g", 120.00, "Pasta", 25),
                    new Product(null, "Olive Oil", "Extra virgin olive oil, 1L", 650.00, "Oil", 15),
                    new Product(null, "Sugar", "Tate & Lyle sugar, 1kg", 55.00, "Baking", 30),
                    new Product(null, "Salt", "Tata salt, 1kg", 25.00, "Spices", 50),
                    new Product(null, "Tea Bags", "Lipton tea bags, 100 pcs", 180.00, "Beverages", 25),
                    new Product(null, "Coffee", "Nescafe instant coffee, 200g", 450.00, "Beverages", 15),
                    new Product(null, "Potato Chips", "Lay's classic chips, 150g", 50.00, "Snacks", 30),
                    new Product(null, "Cookies", "Parle-G biscuits, 800g", 80.00, "Snacks", 22));
            productRepository.saveAll(products);
            System.out.println("✅ Sample products loaded: " + products.size() + " items");
        }
    }
}