package com.bookbridge.controller;

import com.bookbridge.model.Book;
import com.bookbridge.model.Message;
import com.bookbridge.model.User;
import com.bookbridge.service.BookService;
import com.bookbridge.service.MessageService;
import com.bookbridge.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<?> sendMessage(
            @RequestBody Map<String, Object> messageRequest,
            HttpServletRequest request) {
        
        try {
            // Get current user from session
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Long senderId = (Long) session.getAttribute("userId");
            if (senderId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Long receiverId = Long.valueOf(messageRequest.get("receiverId").toString());
            String content = messageRequest.get("content").toString();
            Long bookId = messageRequest.get("bookId") != null ? 
                Long.valueOf(messageRequest.get("bookId").toString()) : null;
            
            Optional<User> senderOpt = userService.getUserById(senderId);
            Optional<User> receiverOpt = userService.getUserById(receiverId);
            
            if (!senderOpt.isPresent() || !receiverOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid sender or receiver"));
            }
            
            Message message = new Message();
            message.setSender(senderOpt.get());
            message.setReceiver(receiverOpt.get());
            message.setContent(content);
            
            if (bookId != null) {
                Optional<Book> bookOpt = bookService.getBookById(bookId);
                if (bookOpt.isPresent()) {
                    message.setBook(bookOpt.get());
                }
            }
            
            Message savedMessage = messageService.saveMessage(message);
            
            return ResponseEntity.ok(Map.of(
                "message", "Message sent successfully",
                "data", savedMessage
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error sending message: " + e.getMessage()));
        }
    }

    @GetMapping("/conversation/{userId}")
    public ResponseEntity<?> getConversation(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        try {
            // Get current user from session
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Long currentUserId = (Long) session.getAttribute("userId");
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Optional<User> currentUserOpt = userService.getUserById(currentUserId);
            Optional<User> otherUserOpt = userService.getUserById(userId);
            
            if (!currentUserOpt.isPresent() || !otherUserOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid users"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messages = messageService.getConversationPaged(
                currentUserOpt.get(), otherUserOpt.get(), pageable);
            
            // Mark messages as read
            messageService.markAllAsRead(currentUserOpt.get(), otherUserOpt.get());
            
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching conversation: " + e.getMessage()));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadMessages(HttpServletRequest request) {
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
            
            List<Message> unreadMessages = messageService.getUnreadMessages(userOpt.get());
            Long unreadCount = messageService.countUnreadMessages(userOpt.get());
            
            return ResponseEntity.ok(Map.of(
                "messages", unreadMessages,
                "count", unreadCount
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching unread messages: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            boolean success = messageService.markAsRead(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Message marked as read"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error marking message as read: " + e.getMessage()));
        }
    }

    @GetMapping("/partners")
    public ResponseEntity<?> getChatPartners(HttpServletRequest request) {
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
            
            List<User> chatPartners = messageService.getChatPartners(userOpt.get());
            return ResponseEntity.ok(chatPartners);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching chat partners: " + e.getMessage()));
        }
    }

    // WebSocket message handling
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Map<String, Object> messagePayload) {
        try {
            Long senderId = Long.valueOf(messagePayload.get("senderId").toString());
            Long receiverId = Long.valueOf(messagePayload.get("receiverId").toString());
            String content = messagePayload.get("content").toString();
            
            Optional<User> senderOpt = userService.getUserById(senderId);
            Optional<User> receiverOpt = userService.getUserById(receiverId);
            
            if (senderOpt.isPresent() && receiverOpt.isPresent()) {
                Message message = new Message();
                message.setSender(senderOpt.get());
                message.setReceiver(receiverOpt.get());
                message.setContent(content);
                
                Message savedMessage = messageService.saveMessage(message);
                
                // Send to receiver via WebSocket
                messagingTemplate.convertAndSendToUser(
                    receiverOpt.get().getEmail(),
                    "/queue/messages",
                    savedMessage
                );
            }
        } catch (Exception e) {
            // Log error
            System.err.println("Error in WebSocket message handling: " + e.getMessage());
        }
    }
}
