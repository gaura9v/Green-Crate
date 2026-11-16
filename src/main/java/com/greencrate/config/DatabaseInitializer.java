package com.greencrate.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;

    public DatabaseInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(255) NOT NULL,
                    description TEXT,
                    price REAL NOT NULL DEFAULT 0,
                    category VARCHAR(100),
                    stock_quantity INTEGER NOT NULL DEFAULT 0
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS carts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at DATETIME NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS cart_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    quantity INTEGER NOT NULL DEFAULT 1,
                    cart_id INTEGER NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
                    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    total_amount REAL NOT NULL DEFAULT 0,
                    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                    created_at DATETIME NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    quantity INTEGER NOT NULL DEFAULT 1,
                    price REAL NOT NULL DEFAULT 0,
                    order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                    product_id INTEGER NOT NULL REFERENCES products(id)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS feedbacks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email VARCHAR(255) NOT NULL,
                    rating INTEGER NOT NULL,
                    comments TEXT,
                    created_at DATETIME NOT NULL
                )
                """);

        if (jdbc.queryForObject("SELECT COUNT(*) FROM products", Integer.class) == 0) {
            seedProducts();
        }
    }

    private void seedProducts() {
        List<Object[]> products = List.of(
                new Object[]{"Fresh Apples", "Crisp and juicy red apples from local farms", 150.0, "Fruits", 50},
                new Object[]{"Milk", "Fresh whole milk, 1 liter pack", 60.0, "Dairy", 30},
                new Object[]{"Whole Wheat Bread", "Freshly baked whole wheat bread", 45.0, "Bakery", 25},
                new Object[]{"Farm Eggs", "Farm fresh eggs, pack of 12", 80.0, "Dairy", 40},
                new Object[]{"Chicken Breast", "Boneless chicken breast, 1kg", 450.0, "Meat", 20},
                new Object[]{"Basmati Rice", "Premium quality basmati rice, 5kg", 350.0, "Grains", 15},
                new Object[]{"Fresh Tomatoes", "Fresh red tomatoes, 1kg", 40.0, "Vegetables", 35},
                new Object[]{"Potatoes", "Fresh potatoes, 5kg bag", 120.0, "Vegetables", 25},
                new Object[]{"Onions", "Fresh yellow onions, 1kg", 35.0, "Vegetables", 40},
                new Object[]{"Carrots", "Fresh organic carrots, 1kg", 50.0, "Vegetables", 30},
                new Object[]{"Greek Yogurt", "Creamy greek yogurt, 500g", 90.0, "Dairy", 20},
                new Object[]{"Butter", "Pure butter, 500g pack", 220.0, "Dairy", 15},
                new Object[]{"Cheddar Cheese", "Aged cheddar cheese, 400g", 350.0, "Dairy", 18},
                new Object[]{"Orange Juice", "Fresh orange juice, 1 liter", 150.0, "Beverages", 22},
                new Object[]{"Green Grapes", "Sweet green grapes, 1kg", 180.0, "Fruits", 28},
                new Object[]{"Strawberries", "Fresh strawberries, 500g", 200.0, "Fruits", 25},
                new Object[]{"Broccoli", "Fresh broccoli, 1kg", 80.0, "Vegetables", 30},
                new Object[]{"Spinach", "Organic spinach leaves, 500g", 45.0, "Vegetables", 35},
                new Object[]{"Cucumber", "Fresh cucumber, 1kg", 30.0, "Vegetables", 40},
                new Object[]{"Corn Flakes", "Kellogg's corn flakes, 375g", 250.0, "Breakfast", 20},
                new Object[]{"Oatmeal", "Quaker oats, 1kg", 280.0, "Breakfast", 18},
                new Object[]{"Pasta", "Barilla penne pasta, 500g", 120.0, "Pasta", 25},
                new Object[]{"Olive Oil", "Extra virgin olive oil, 1L", 650.0, "Oil", 15},
                new Object[]{"Sugar", "Tate & Lyle sugar, 1kg", 55.0, "Baking", 30},
                new Object[]{"Salt", "Tata salt, 1kg", 25.0, "Spices", 50},
                new Object[]{"Tea Bags", "Lipton tea bags, 100 pcs", 180.0, "Beverages", 25},
                new Object[]{"Coffee", "Nescafe instant coffee, 200g", 450.0, "Beverages", 15},
                new Object[]{"Potato Chips", "Lay's classic chips, 150g", 50.0, "Snacks", 30},
                new Object[]{"Cookies", "Parle-G biscuits, 800g", 80.0, "Snacks", 22}
        );
        for (Object[] product : products) {
            jdbc.update("""
                    INSERT INTO products (name, description, price, category, stock_quantity)
                    VALUES (?, ?, ?, ?, ?)
                    """, product);
        }
    }
}
