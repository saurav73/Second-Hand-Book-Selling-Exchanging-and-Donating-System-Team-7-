package com.bookbridge.controller;

import com.bookbridge.model.CartItem;
import com.bookbridge.model.User;
import com.bookbridge.service.CartService;
import com.bookbridge.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getCartItems(HttpServletRequest request) {
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
            
            List<CartItem> cartItems = cartService.getCartItems(userOpt.get());
            Double total = cartService.calculateCartTotal(userOpt.get());
            Long itemCount = cartService.countCartItems(userOpt.get());
            
            return ResponseEntity.ok(Map.of(
                "items", cartItems,
                "total", total,
                "itemCount", itemCount
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching cart items: " + e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestBody Map<String, Object> cartRequest,
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
            
            Long bookId = Long.valueOf(cartRequest.get("bookId").toString());
            Integer quantity = cartRequest.get("quantity") != null ? 
                Integer.valueOf(cartRequest.get("quantity").toString()) : 1;
            
            CartItem cartItem = cartService.addToCart(userOpt.get(), bookId, quantity);
            
            return ResponseEntity.ok(Map.of(
                "message", "Item added to cart successfully",
                "cartItem", cartItem
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error adding item to cart: " + e.getMessage()));
        }
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Object> updateRequest) {
        
        try {
            Integer quantity = Integer.valueOf(updateRequest.get("quantity").toString());
            CartItem updatedItem = cartService.updateCartItemQuantity(cartItemId, quantity);
            
            return ResponseEntity.ok(Map.of(
                "message", "Cart item updated successfully",
                "cartItem", updatedItem
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating cart item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId) {
        try {
            cartService.removeFromCart(cartItemId);
            return ResponseEntity.ok(Map.of("message", "Item removed from cart successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error removing item from cart: " + e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
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
            
            cartService.clearCart(userOpt.get());
            return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error clearing cart: " + e.getMessage()));
        }
    }
}
