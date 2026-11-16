package com.greencrate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.test.web.servlet.MvcResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite:./target/test-greencrate.sqlite3",
        "spring.mail.username=",
        "spring.mail.password="
})
@AutoConfigureMockMvc
class GreenCrateApiTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    private long productId;

    @BeforeEach
    void resetDatabase() {
        jdbc.update("DELETE FROM feedbacks");
        jdbc.update("DELETE FROM order_items");
        jdbc.update("DELETE FROM orders");
        jdbc.update("DELETE FROM cart_items");
        jdbc.update("DELETE FROM carts");
        jdbc.update("DELETE FROM products");
        jdbc.update("""
                INSERT INTO products (name, description, price, category, stock_quantity)
                VALUES ('Organic Bananas', 'Fresh bananas', 60, 'Fruits', 100)
                """);
        productId = jdbc.queryForObject("SELECT last_insert_rowid()", Long.class);
    }

    @Test
    void productEndpointsKeepTheFrontendContract() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stockQuantity").value(100));

        mvc.perform(get("/api/products/category/fruits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Organic Bananas"));

        mvc.perform(put("/api/products/{id}", productId)
                        .contentType("application/json")
                        .content("""
                                {"name":"Premium Bananas","price":75,"stockQuantity":80}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Premium Bananas"))
                .andExpect(jsonPath("$.stockQuantity").value(80));

        String created = mvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("""
                                {"name":"Almond Milk","price":180,"category":"Dairy","stockQuantity":20}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(20))
                .andReturn().getResponse().getContentAsString();
        long createdId = objectMapper.readTree(created).get("id").asLong();
        mvc.perform(delete("/api/products/{id}", createdId)).andExpect(status().isOk());
    }

    @Test
    void cartCheckoutCreatesAnOrderAndEmptiesTheCart() throws Exception {
        String cartResponse = mvc.perform(post("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andReturn().getResponse().getContentAsString();
        long cartId = objectMapper.readTree(cartResponse).get("id").asLong();

        mvc.perform(post("/api/cart/{id}/add", cartId)
                        .contentType("application/json")
                        .content("""
                                {"product_id":%d,"quantity":2}
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        MvcResult checkout = mvc.perform(post("/api/orders/checkout/{id}", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(120.0))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andReturn();
        JsonNode order = objectMapper.readTree(checkout.getResponse().getContentAsString());
        long orderId = order.get("id").asLong();

        mvc.perform(get("/api/cart/{id}", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));

        mvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));

        mvc.perform(put("/api/orders/{id}/status", orderId).param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mvc.perform(delete("/api/orders/{id}", orderId)).andExpect(status().isOk());
    }

    @Test
    void feedbackAndStorefrontAreServed() throws Exception {
        mvc.perform(post("/api/feedback")
                        .contentType("application/json")
                        .content("""
                                {"email":"customer@example.com","rating":5,"comments":"Great produce"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("customer@example.com"))
                .andExpect(jsonPath("$.createdAt").exists());

        mvc.perform(get("/api/feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/home.html"))
                .andExpect(status().isOk());
    }
}
