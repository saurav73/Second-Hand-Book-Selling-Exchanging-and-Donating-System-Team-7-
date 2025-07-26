package com.bookbridge.controller;

import com.bookbridge.model.Book;
import com.bookbridge.model.User;
import com.bookbridge.service.BookService;
import com.bookbridge.service.FileStorageService;
import com.bookbridge.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<?> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String listingType,
            @RequestParam(required = false) String location) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Book> books;
            
            if (keyword != null || category != null || condition != null || listingType != null || location != null) {
                Book.BookCategory bookCategory = category != null ? Book.BookCategory.valueOf(category.toUpperCase()) : null;
                Book.BookCondition bookCondition = condition != null ? Book.BookCondition.valueOf(condition.toUpperCase()) : null;
                Book.ListingType bookListingType = listingType != null ? Book.ListingType.valueOf(listingType.toUpperCase()) : null;
                
                books = bookService.searchBooksWithFilters(
                    keyword != null ? keyword : "",
                    bookCategory,
                    bookCondition,
                    bookListingType,
                    location != null ? location : "",
                    pageable
                );
            } else {
                books = bookService.getAvailableBooks(pageable);
            }
            
            return ResponseEntity.ok(books);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching books: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        Optional<Book> bookOpt = bookService.getBookById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            // Increment view count
            bookService.incrementViewCount(id);
            return ResponseEntity.ok(book);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createBook(
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("category") String category,
            @RequestParam("condition") String condition,
            @RequestParam("listingType") String listingType,
            @RequestParam("location") String location,
            @RequestParam(value = "edition", required = false) String edition,
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "price", required = false) String price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "bookImage", required = false) MultipartFile bookImage,
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
            
            User user = userOpt.get();
            
            // Create book
            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setCategory(Book.BookCategory.valueOf(category.toUpperCase()));
            book.setCondition(Book.BookCondition.valueOf(condition.toUpperCase()));
            book.setListingType(Book.ListingType.valueOf(listingType.toUpperCase()));
            book.setLocation(location);
            book.setEdition(edition);
            book.setIsbn(isbn);
            book.setDescription(description);
            book.setUser(user);
            
            // Set price if listing type is SELL
            if (book.getListingType() == Book.ListingType.SELL && price != null) {
                book.setPrice(new BigDecimal(price));
            }
            
            // Store book image if provided
            if (bookImage != null && !bookImage.isEmpty()) {
                String imagePath = fileStorageService.storeBookImage(bookImage);
                book.setBookImage(imagePath);
            }
            
            Book savedBook = bookService.createBook(book);
            
            return ResponseEntity.ok(Map.of(
                "message", "Book created successfully",
                "book", savedBook
            ));
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload book image"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creating book: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("category") String category,
            @RequestParam("condition") String condition,
            @RequestParam("listingType") String listingType,
            @RequestParam("location") String location,
            @RequestParam(value = "edition", required = false) String edition,
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "price", required = false) String price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "bookImage", required = false) MultipartFile bookImage,
            HttpServletRequest request) {
        
        try {
            // Get current user from session
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Long userId = (Long) session.getAttribute("userId");
            Optional<Book> bookOpt = bookService.getBookById(id);
            
            if (!bookOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Book book = bookOpt.get();
            
            // Check if user owns this book
            if (!book.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only edit your own books"));
            }
            
            // Update book details
            book.setTitle(title);
            book.setAuthor(author);
            book.setCategory(Book.BookCategory.valueOf(category.toUpperCase()));
            book.setCondition(Book.BookCondition.valueOf(condition.toUpperCase()));
            book.setListingType(Book.ListingType.valueOf(listingType.toUpperCase()));
            book.setLocation(location);
            book.setEdition(edition);
            book.setIsbn(isbn);
            book.setDescription(description);
            
            // Set price if listing type is SELL
            if (book.getListingType() == Book.ListingType.SELL && price != null) {
                book.setPrice(new BigDecimal(price));
            } else {
                book.setPrice(null);
            }
            
            // Update book image if provided
            if (bookImage != null && !bookImage.isEmpty()) {
                // Delete old image if exists
                if (book.getBookImage() != null) {
                    fileStorageService.deleteFile(book.getBookImage());
                }
                String imagePath = fileStorageService.storeBookImage(bookImage);
                book.setBookImage(imagePath);
            }
            
            Book updatedBook = bookService.updateBook(book);
            
            return ResponseEntity.ok(Map.of(
                "message", "Book updated successfully",
                "book", updatedBook
            ));
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload book image"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating book: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id, HttpServletRequest request) {
        try {
            // Get current user from session
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Not authenticated"));
            }
            
            Long userId = (Long) session.getAttribute("userId");
            Optional<Book> bookOpt = bookService.getBookById(id);
            
            if (!bookOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Book book = bookOpt.get();
            
            // Check if user owns this book
            if (!book.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only delete your own books"));
            }
            
            boolean deleted = bookService.deleteBook(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Book deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Failed to delete book"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error deleting book: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBooks(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userService.getUserById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Book> books = bookService.getBooksByUser(userOpt.get());
            return ResponseEntity.ok(books);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching user books: " + e.getMessage()));
        }
    }

    @GetMapping("/my-books")
    public ResponseEntity<?> getMyBooks(HttpServletRequest request) {
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
            
            List<Book> books = bookService.getBooksByUser(userOpt.get());
            return ResponseEntity.ok(books);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching your books: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/interest")
    public ResponseEntity<?> expressInterest(@PathVariable Long id) {
        try {
            bookService.incrementInterestCount(id);
            return ResponseEntity.ok(Map.of("message", "Interest recorded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error recording interest: " + e.getMessage()));
        }
    }
}
