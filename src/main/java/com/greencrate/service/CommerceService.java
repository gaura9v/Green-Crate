package com.greencrate.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Statement;

import org.springframework.web.server.ResponseStatusException;

@Service
public class CommerceService {
    private static final DateTimeFormatter DATABASE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter API_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    public CommerceService(JdbcTemplate jdbc, TransactionTemplate transactions) {
        this.jdbc = jdbc;
        this.transactions = transactions;
    }

    public List<Map<String, Object>> products() {
        return jdbc.query("SELECT * FROM products ORDER BY id", productMapper());
    }

    public Map<String, Object> product(long id) {
        return jdbc.query("SELECT * FROM products WHERE id = ?", productMapper(), id)
                .stream().findFirst().orElseThrow(() -> notFound("Product not found"));
    }

    public List<Map<String, Object>> productsByCategory(String category) {
        return jdbc.query("SELECT * FROM products WHERE LOWER(category) = LOWER(?) ORDER BY id",
                productMapper(), category);
    }

    public Map<String, Object> createProduct(Map<String, Object> data) {
        String name = requiredText(data.get("name"), "name");
        long id = insert("""
                INSERT INTO products (name, description, price, category, stock_quantity)
                VALUES (?, ?, ?, ?, ?)
                """, name, data.get("description"), decimal(data.get("price"), 0),
                data.get("category"), integer(data.get("stockQuantity"), 0));
        return product(id);
    }

    public Map<String, Object> updateProduct(long id, Map<String, Object> data) {
        Map<String, Object> current = product(id);
        String name = data.containsKey("name") ? requiredText(data.get("name"), "name")
                : (String) current.get("name");
        Object description = data.getOrDefault("description", current.get("description"));
        double price = data.containsKey("price") ? decimal(data.get("price"), 0)
                : ((Number) current.get("price")).doubleValue();
        Object category = data.getOrDefault("category", current.get("category"));
        int stock = data.containsKey("stockQuantity")
                ? integer(data.get("stockQuantity"), 0)
                : ((Number) current.get("stockQuantity")).intValue();
        jdbc.update("""
                UPDATE products SET name = ?, description = ?, price = ?, category = ?,
                    stock_quantity = ? WHERE id = ?
                """, name, description, price, category, stock, id);
        return product(id);
    }

    public void deleteProduct(long id) {
        product(id);
        jdbc.update("DELETE FROM cart_items WHERE product_id = ?", id);
        jdbc.update("DELETE FROM order_items WHERE product_id = ?", id);
        jdbc.update("DELETE FROM products WHERE id = ?", id);
    }

    public Map<String, Object> createCart() {
        long id = insert("INSERT INTO carts (created_at) VALUES (?)", now());
        return cart(id);
    }

    public Map<String, Object> cart(long id) {
        if (!exists("SELECT COUNT(*) FROM carts WHERE id = ?", id)) {
            return createCart();
        }
        return cartMap(id);
    }

    public Map<String, Object> addToCart(long cartId, String productId, String quantityValue) {
        if (productId == null || productId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId is required");
        }
        long parsedProductId;
        try {
            parsedProductId = Long.parseLong(productId);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId must be an integer");
        }
        product(parsedProductId);
        int quantity = 1;
        try {
            if (quantityValue != null) {
                quantity = Integer.parseInt(quantityValue);
            }
        } catch (NumberFormatException ignored) {
            quantity = 1;
        }
        if (!exists("SELECT COUNT(*) FROM carts WHERE id = ?", cartId)) {
            jdbc.update("INSERT INTO carts (id, created_at) VALUES (?, ?)", cartId, now());
        }
        List<Long> itemIds = jdbc.query("SELECT id FROM cart_items WHERE cart_id = ? AND product_id = ?",
                (result, row) -> result.getLong(1), cartId, parsedProductId);
        if (itemIds.isEmpty()) {
            jdbc.update("INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)",
                    cartId, parsedProductId, quantity);
        } else {
            jdbc.update("UPDATE cart_items SET quantity = quantity + ? WHERE id = ?",
                    quantity, itemIds.get(0));
        }
        return cartMap(cartId);
    }

    public Map<String, Object> removeCartItem(long cartId, long itemId) {
        if (!exists("SELECT COUNT(*) FROM carts WHERE id = ?", cartId)) {
            throw notFound("Cart not found");
        }
        jdbc.update("DELETE FROM cart_items WHERE cart_id = ? AND id = ?", cartId, itemId);
        return cartMap(cartId);
    }

    public List<Map<String, Object>> orders() {
        List<Long> ids = jdbc.query("SELECT id FROM orders ORDER BY id DESC",
                (result, row) -> result.getLong(1));
        return ids.stream().map(this::order).toList();
    }

    public Map<String, Object> order(long id) {
        Map<String, Object> order = jdbc.query("""
                SELECT id, total_amount, status FROM orders WHERE id = ?
                """, (result, row) -> map(
                        "id", result.getLong("id"),
                        "totalAmount", result.getDouble("total_amount"),
                        "status", result.getString("status")), id)
                .stream().findFirst().orElseThrow(() -> notFound("Order not found"));
        List<Map<String, Object>> items = jdbc.query("""
                SELECT oi.id, oi.quantity, oi.price,
                       p.id AS product_id, p.name, p.description, p.price AS product_price,
                       p.category, p.stock_quantity
                FROM order_items oi JOIN products p ON p.id = oi.product_id
                WHERE oi.order_id = ? ORDER BY oi.id
                """, (result, row) -> map(
                        "id", result.getLong("id"),
                        "product", map(
                                "id", result.getLong("product_id"),
                                "name", result.getString("name"),
                                "description", result.getString("description"),
                                "price", result.getDouble("product_price"),
                                "category", result.getString("category"),
                                "stockQuantity", result.getInt("stock_quantity")),
                        "quantity", result.getInt("quantity"),
                        "price", result.getDouble("price")), id);
        order.put("items", items);
        return order;
    }

    public Map<String, Object> checkout(long cartId) {
        return Objects.requireNonNull(transactions.execute(status -> {
            if (!exists("SELECT COUNT(*) FROM carts WHERE id = ?", cartId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
            }
            List<Map<String, Object>> items = jdbc.query("""
                    SELECT ci.product_id, ci.quantity, p.price
                    FROM cart_items ci JOIN products p ON p.id = ci.product_id
                    WHERE ci.cart_id = ? ORDER BY ci.id
                    """, (result, row) -> map(
                            "productId", result.getLong("product_id"),
                            "quantity", result.getInt("quantity"),
                            "price", result.getDouble("price")), cartId);
            if (items.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
            }
            double total = items.stream().mapToDouble(item ->
                    ((Number) item.get("price")).doubleValue()
                            * ((Number) item.get("quantity")).intValue()).sum();
            long orderId = insert("""
                    INSERT INTO orders (total_amount, status, created_at) VALUES (?, 'PENDING', ?)
                    """, total, now());
            for (Map<String, Object> item : items) {
                jdbc.update("""
                        INSERT INTO order_items (order_id, product_id, quantity, price)
                        VALUES (?, ?, ?, ?)
                        """, orderId, item.get("productId"), item.get("quantity"), item.get("price"));
            }
            jdbc.update("DELETE FROM cart_items WHERE cart_id = ?", cartId);
            return order(orderId);
        }));
    }

    public Map<String, Object> updateOrderStatus(long id, String status) {
        order(id);
        if (status != null && !status.isBlank()) {
            jdbc.update("UPDATE orders SET status = ? WHERE id = ?", status, id);
        }
        return order(id);
    }

    public void deleteOrder(long id) {
        order(id);
        jdbc.update("DELETE FROM order_items WHERE order_id = ?", id);
        jdbc.update("DELETE FROM orders WHERE id = ?", id);
    }

    public List<Map<String, Object>> feedback() {
        return jdbc.query("""
                SELECT id, email, rating, comments, created_at FROM feedbacks ORDER BY id
                """, feedbackMapper());
    }

    public Map<String, Object> createFeedback(Map<String, Object> data) {
        String email = requiredText(data.get("email"), "email");
        int rating = integer(data.get("rating"), 0);
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rating must be between 1 and 5");
        }
        long id = insert("""
                INSERT INTO feedbacks (email, rating, comments, created_at) VALUES (?, ?, ?, ?)
                """, email, rating, data.get("comments"), now());
        return jdbc.query("""
                SELECT id, email, rating, comments, created_at FROM feedbacks WHERE id = ?
                """, feedbackMapper(), id).get(0);
    }

    private Map<String, Object> cartMap(long id) {
        List<Map<String, Object>> items = jdbc.query("""
                SELECT ci.id, ci.quantity, p.id AS product_id, p.name, p.description,
                       p.price, p.category, p.stock_quantity
                FROM cart_items ci JOIN products p ON p.id = ci.product_id
                WHERE ci.cart_id = ? ORDER BY ci.id
                """, (result, row) -> map(
                        "id", result.getLong("id"),
                        "product", map(
                                "id", result.getLong("product_id"),
                                "name", result.getString("name"),
                                "description", result.getString("description"),
                                "price", result.getDouble("price"),
                                "category", result.getString("category"),
                                "stockQuantity", result.getInt("stock_quantity")),
                        "quantity", result.getInt("quantity")), id);
        return map("id", id, "items", items);
    }

    private RowMapper<Map<String, Object>> productMapper() {
        return (result, row) -> map(
                "id", result.getLong("id"),
                "name", result.getString("name"),
                "description", result.getString("description"),
                "price", result.getDouble("price"),
                "category", result.getString("category"),
                "stockQuantity", result.getInt("stock_quantity"));
    }

    private RowMapper<Map<String, Object>> feedbackMapper() {
        return (result, row) -> map(
                "id", result.getLong("id"),
                "email", result.getString("email"),
                "rating", result.getInt("rating"),
                "comments", result.getString("comments"),
                "createdAt", apiTime(result.getString("created_at")));
    }

    private long insert(String sql, Object... values) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Insert did not return a generated key");
        }
        return key.longValue();
    }

    private boolean exists(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args) > 0;
    }

    private static String now() {
        return LocalDateTime.now().format(DATABASE_TIME);
    }

    private static String apiTime(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        try {
            return LocalDateTime.parse(value.replace('T', ' ').substring(0, 19), DATABASE_TIME)
                    .format(API_TIME);
        } catch (RuntimeException ignored) {
            return value.replace(' ', 'T');
        }
    }

    private static double decimal(Object value, double fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price must be a number");
        }
    }

    private static int integer(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value must be an integer");
        }
    }

    private static String requiredText(Object value, String field) {
        if (value == null || value.toString().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.toString().trim();
    }

    private static Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put((String) pairs[i], pairs[i + 1]);
        }
        return result;
    }

    private static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
