package com.bookbridge.service;

import com.bookbridge.model.*;
import com.bookbridge.repository.BookRepository;
import com.bookbridge.repository.CartItemRepository;
import com.bookbridge.repository.OrderItemRepository;
import com.bookbridge.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private BookRepository bookRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Page<Order> getUserOrdersPaged(User user, Pageable pageable) {
        return orderRepository.findUserOrdersPaged(user, pageable);
    }

    @Transactional
    public Order createOrderFromCart(User user, String deliveryAddress, String deliveryPhone, String deliveryNotes) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            if (item.getBook().getListingType() == Book.ListingType.SELL && item.getBook().getPrice() != null) {
                totalAmount = totalAmount.add(item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        
        // Create order
        Order order = new Order(user, totalAmount, deliveryAddress);
        order.setDeliveryPhone(deliveryPhone);
        order.setDeliveryNotes(deliveryNotes);
        order.setEstimatedDelivery(LocalDateTime.now().plusDays(7)); // Default 7 days delivery estimate
        Order savedOrder = orderRepository.save(order);
        
        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            
            // Update book status
            book.setStatus(Book.BookStatus.RESERVED);
            bookRepository.save(book);
            
            // Create order item
            BigDecimal unitPrice = (book.getListingType() == Book.ListingType.SELL && book.getPrice() != null) 
                ? book.getPrice() : BigDecimal.ZERO;
            OrderItem orderItem = new OrderItem(savedOrder, book, cartItem.getQuantity(), unitPrice);
            orderItems.add(orderItemRepository.save(orderItem));
        }
        
        // Clear cart
        cartItemRepository.deleteByUser(user);
        
        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            
            if (status == Order.OrderStatus.DELIVERED) {
                order.setCompletedAt(LocalDateTime.now());
                
                // Update book statuses
                List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                for (OrderItem item : orderItems) {
                    Book book = item.getBook();
                    switch (book.getListingType()) {
                        case SELL:
                            book.setStatus(Book.BookStatus.SOLD);
                            break;
                        case EXCHANGE:
                            book.setStatus(Book.BookStatus.EXCHANGED);
                            break;
                        case DONATE:
                            book.setStatus(Book.BookStatus.DONATED);
                            break;
                    }
                    bookRepository.save(book);
                }
            }
            
            return orderRepository.save(order);
        }
        throw new IllegalArgumentException("Order not found");
    }

    @Transactional
    public Order updateDeliveryStatus(Long orderId, Order.DeliveryStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setDeliveryStatus(status);
            return orderRepository.save(order);
        }
        throw new IllegalArgumentException("Order not found");
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(Order.OrderStatus.CANCELLED);
            
            // Update book statuses back to available
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            for (OrderItem item : orderItems) {
                Book book = item.getBook();
                book.setStatus(Book.BookStatus.AVAILABLE);
                bookRepository.save(book);
            }
            
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Order not found");
        }
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByDeliveryStatus(Order.DeliveryStatus deliveryStatus) {
        return orderRepository.findByDeliveryStatus(deliveryStatus);
    }

    public Long countOrdersCreatedAfter(LocalDateTime date) {
        return orderRepository.countOrdersCreatedAfter(date);
    }

    public Long countOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.countOrdersByStatus(status);
    }

    public Double sumOrderAmountBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        Double sum = orderRepository.sumOrderAmountBetweenDates(startDate, endDate);
        return sum != null ? sum : 0.0;
    }
}
