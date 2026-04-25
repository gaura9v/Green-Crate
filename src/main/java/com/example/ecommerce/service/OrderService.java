package com.example.ecommerce.service;

import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;

    public Order createOrderFromCart(Long cartId) {
        Cart cart = cartService.getCart(cartId);
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setStatus("PENDING");
        double totalAmount = 0;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            order.addItem(orderItem);
            totalAmount += (orderItem.getPrice() * orderItem.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        // Clear the cart after creating order
        cartService.clearCart(cart);
        
        return savedOrder;
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
