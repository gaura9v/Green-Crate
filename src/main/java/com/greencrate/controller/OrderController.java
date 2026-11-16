package com.greencrate.controller;

import java.util.List;
import java.util.Map;

import com.greencrate.service.CommerceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final CommerceService commerce;

    public OrderController(CommerceService commerce) {
        this.commerce = commerce;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return commerce.orders();
    }

    @PostMapping("/checkout/{cartId}")
    public Map<String, Object> checkout(@PathVariable long cartId) {
        return commerce.checkout(cartId);
    }

    @GetMapping("/{orderId}")
    public Map<String, Object> get(@PathVariable long orderId) {
        return commerce.order(orderId);
    }

    @PutMapping("/{orderId}/status")
    public Map<String, Object> updateStatus(
            @PathVariable long orderId,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, Object> body) {
        String requestedStatus = status != null ? status
                : body == null || body.get("status") == null ? null : body.get("status").toString();
        return commerce.updateOrderStatus(orderId, requestedStatus);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> delete(@PathVariable long orderId) {
        commerce.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
