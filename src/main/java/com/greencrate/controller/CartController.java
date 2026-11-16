package com.greencrate.controller;

import java.util.Map;

import com.greencrate.service.CommerceService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CommerceService commerce;

    public CartController(CommerceService commerce) {
        this.commerce = commerce;
    }

    @PostMapping
    public Map<String, Object> create() {
        return commerce.createCart();
    }

    @GetMapping("/{cartId}")
    public Map<String, Object> get(@PathVariable long cartId) {
        return commerce.cart(cartId);
    }

    @PostMapping("/{cartId}/add")
    public Map<String, Object> add(
            @PathVariable long cartId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String quantity,
            @org.springframework.web.bind.annotation.RequestBody(required = false) Map<String, Object> body) {
        String requestedProductId = productId != null ? productId
                : body == null ? null : value(body.get("productId"), body.get("product_id"));
        String requestedQuantity = quantity != null ? quantity
                : body == null || body.get("quantity") == null ? null : body.get("quantity").toString();
        return commerce.addToCart(cartId, requestedProductId, requestedQuantity);
    }

    @DeleteMapping("/{cartId}/item/{itemId}")
    public Map<String, Object> remove(@PathVariable long cartId, @PathVariable long itemId) {
        return commerce.removeCartItem(cartId, itemId);
    }

    private static String value(Object preferred, Object fallback) {
        Object selected = preferred == null ? fallback : preferred;
        return selected == null ? null : selected.toString();
    }
}
