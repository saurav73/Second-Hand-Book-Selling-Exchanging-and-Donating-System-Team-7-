package com.bookbridge.controller;

import com.bookbridge.model.Order;
import com.bookbridge.model.User;
import com.bookbridge.service.EmailService;
import com.bookbridge.service.OrderService;
import com.bookbridge.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;

    @GetMapping
    public ResponseEntity<?> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        try {
            // Get current user from session
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Optional<User> userOpt = userService.getUserById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not found"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getUserOrdersPaged(userOpt.get(), pageable);
            
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching orders: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id, HttpServletRequest request) {
        try {
            // Get current user from session
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Long userId = (Long) session.getAttribute("userId");
            Optional<Order> orderOpt = orderService.getOrderById(id);
            
            if (!orderOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Order order = orderOpt.get();
            
            // Check if user owns this order
            if (!order.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only view your own orders"));
            }
            
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching order: " + e.getMessage()));
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> createOrder(
            @RequestBody Map<String, Object> orderRequest,
            HttpServletRequest request) {
        
        try {
            // Get current user from session
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Optional<User> userOpt = userService.getUserById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not found"));
            }
            
            String deliveryAddress = orderRequest.get("deliveryAddress").toString();
            String deliveryPhone = orderRequest.get("deliveryPhone") != null ? 
                orderRequest.get("deliveryPhone").toString() : null;
            String deliveryNotes = orderRequest.get("deliveryNotes") != null ? 
                orderRequest.get("deliveryNotes").toString() : null;
            
            Order order = orderService.createOrderFromCart(
                userOpt.get(), deliveryAddress, deliveryPhone, deliveryNotes);
            
            // Send order confirmation email
            emailService.sendOrderConfirmationEmail(
                order.getUser().getEmail(),
                order.getUser().getFullName(),
                order.getOrderNumber()
            );
            
            return ResponseEntity.ok(Map.of(
                "message", "Order created successfully",
                "order", order
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creating order: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        try {
            // Get current user from session
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Long userId = (Long) session.getAttribute("userId");
            Optional<Order> orderOpt = orderService.getOrderById(id);
            
            if (!orderOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Order order = orderOpt.get();
            
            // Check if user owns this order
            if (!order.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only cancel your own orders"));
            }
            
            // Check if order can be cancelled
            if (order.getStatus() == Order.OrderStatus.DELIVERED || 
                order.getStatus() == Order.OrderStatus.CANCELLED) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Order cannot be cancelled"));
            }
            
            orderService.cancelOrder(id);
            
            return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error cancelling order: " + e.getMessage()));
        }
    }
}
