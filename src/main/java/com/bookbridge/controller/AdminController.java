package com.bookbridge.controller;

import com.bookbridge.model.*;
import com.bookbridge.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private UpworkTransactionService upworkTransactionService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Check if user is admin
            if (user.getUserType() != User.UserType.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Access denied. Admin privileges required."));
            }
            
            // Verify password
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Store admin session
                HttpSession session = request.getSession();
                session.setAttribute("adminId", user.getId());
                session.setAttribute("isAdmin", true);
                
                return ResponseEntity.ok(Map.of(
                    "message", "Admin login successful",
                    "admin", Map.of(
                        "id", user.getId(),
                        "fullName", user.getFullName(),
                        "email", user.getEmail()
                    )
                ));
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid admin credentials"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats(HttpServletRequest request) {
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
            LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
            
            Map<String, Object> stats = new HashMap<>();
            
            // User statistics
            List<User> allUsers = userService.getAllUsers();
            stats.put("totalUsers", allUsers.size());
            stats.put("newUsersThisMonth", userService.countUsersCreatedAfter(lastMonth));
            stats.put("activeUsers", allUsers.stream().filter(u -> u.getStatus() == User.UserStatus.ACTIVE).count());
            
            // Book statistics
            stats.put("totalBooks", bookService.countBooksByStatus(Book.BookStatus.AVAILABLE));
            stats.put("newBooksThisMonth", bookService.countBooksCreatedAfter(lastMonth));
            stats.put("soldBooks", bookService.countBooksByStatus(Book.BookStatus.SOLD));
            
            // Order statistics
            stats.put("totalOrders", orderService.countOrdersByStatus(Order.OrderStatus.DELIVERED));
            stats.put("newOrdersThisWeek", orderService.countOrdersCreatedAfter(lastWeek));
            stats.put("pendingOrders", orderService.countOrdersByStatus(Order.OrderStatus.PENDING));
            
            // Payment statistics
            stats.put("totalPayments", paymentService.countPaymentsByStatus(Payment.PaymentStatus.SUCCESS));
            stats.put("totalRevenue", paymentService.sumSuccessfulPaymentsBetweenDates(
                LocalDateTime.now().minusYears(1), LocalDateTime.now()));
            stats.put("revenueThisMonth", paymentService.sumSuccessfulPaymentsBetweenDates(lastMonth, LocalDateTime.now()));
            
            // Upwork transactions
            stats.put("upworkTransactions", upworkTransactionService.sumCompletedTransactions());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching dashboard stats: " + e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching users: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            boolean success = userService.blockUser(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "User blocked successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error blocking user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            boolean success = userService.unblockUser(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "User unblocked successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error unblocking user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error deleting user: " + e.getMessage()));
        }
    }

    @GetMapping("/books")
    public ResponseEntity<?> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            List<Book> books = bookService.getAllBooks();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching books: " + e.getMessage()));
        }
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            boolean success = bookService.hardDeleteBook(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Book deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error deleting book: " + e.getMessage()));
        }
    }

    @GetMapping("/payments")
    public ResponseEntity<?> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Payment> payments = paymentService.getAllPaymentsPaged(pageable);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching payments: " + e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(HttpServletRequest request) {
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching orders: " + e.getMessage()));
        }
    }

    @PostMapping("/upwork")
    public ResponseEntity<?> logUpworkTransaction(
            @RequestBody Map<String, Object> transactionRequest,
            HttpServletRequest request) {
        
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            String transactionId = transactionRequest.get("transactionId").toString();
            String projectName = transactionRequest.get("projectName") != null ? 
                transactionRequest.get("projectName").toString() : null;
            BigDecimal amount = transactionRequest.get("amount") != null ? 
                new BigDecimal(transactionRequest.get("amount").toString()) : null;
            String description = transactionRequest.get("description") != null ? 
                transactionRequest.get("description").toString() : null;
            
            UpworkTransaction transaction = upworkTransactionService.createTransaction(
                transactionId, projectName, amount, description);
            
            return ResponseEntity.ok(Map.of(
                "message", "Upwork transaction logged successfully",
                "transaction", transaction
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error logging Upwork transaction: " + e.getMessage()));
        }
    }

    @GetMapping("/upwork")
    public ResponseEntity<?> getUpworkTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UpworkTransaction> transactions = upworkTransactionService.getAllTransactionsPaged(pageable);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching Upwork transactions: " + e.getMessage()));
        }
    }

    @PutMapping("/upwork/{id}/status")
    public ResponseEntity<?> updateUpworkTransactionStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusRequest,
            HttpServletRequest request) {
        
        if (!isAdminAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Admin authentication required"));
        }
        
        try {
            String status = statusRequest.get("status");
            UpworkTransaction.TransactionStatus transactionStatus = 
                UpworkTransaction.TransactionStatus.valueOf(status.toUpperCase());
            
            UpworkTransaction transaction = upworkTransactionService.updateTransactionStatus(id, transactionStatus);
            
            return ResponseEntity.ok(Map.of(
                "message", "Transaction status updated successfully",
                "transaction", transaction
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating transaction status: " + e.getMessage()));
        }
    }

    private boolean isAdminAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            Long adminId = (Long) session.getAttribute("adminId");
            return isAdmin != null && isAdmin && adminId != null;
        }
        return false;
    }
}
