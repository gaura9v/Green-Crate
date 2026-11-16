package com.greencrate.controller;

import java.util.List;
import java.util.Map;

import com.greencrate.service.CommerceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final CommerceService commerce;

    public ProductController(CommerceService commerce) {
        this.commerce = commerce;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return commerce.products();
    }

    @GetMapping("/category/{category}")
    public List<Map<String, Object>> byCategory(@PathVariable String category) {
        return commerce.productsByCategory(category);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable long id) {
        return commerce.product(id);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        return commerce.createProduct(data);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable long id, @RequestBody Map<String, Object> data) {
        return commerce.updateProduct(id, data);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        commerce.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
