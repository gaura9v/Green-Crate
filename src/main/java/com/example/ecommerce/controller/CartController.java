package com.example.ecommerce.controller;

import com.example.ecommerce.model.Cart;
import com.example.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<Cart> createCart() {
        return ResponseEntity.ok(cartService.createCart());
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCart(cartId));
    }

    @PostMapping("/{cartId}/add")
    public ResponseEntity<Cart> addProductToCart(
            @PathVariable Long cartId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addProductToCart(cartId, productId, quantity));
    }

    @DeleteMapping("/{cartId}/item/{itemId}")
    public ResponseEntity<Cart> removeItemFromCart(
            @PathVariable Long cartId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(cartId, itemId));
    }
}
