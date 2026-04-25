package com.example.ecommerce.service;

import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductService productService;

    public Cart createCart() {
        Cart newCart = new Cart();
        return cartRepository.save(newCart);
    }

    public Cart getCart(Long cartId) {
        return cartRepository.findById(cartId).orElseGet(() -> {
            Cart newCart = new Cart();
            return cartRepository.save(newCart);
        });
    }

    public Cart addProductToCart(Long cartId, Long productId, Integer quantity) {
        Cart cart = getCart(cartId);
        Product product = productService.getProductById(productId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.addItem(newItem);
        }

        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(Long cartId, Long itemId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return cartRepository.save(cart);
    }

    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
