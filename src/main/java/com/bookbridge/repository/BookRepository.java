package com.bookbridge.repository;

import com.bookbridge.model.Book;
import com.bookbridge.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByUser(User user);
    
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE'")
    Page<Book> findAvailableBooks(Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', ?1, '%')))")
    Page<Book> searchBooks(String keyword, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND " +
           "b.category = ?1")
    Page<Book> findByCategory(Book.BookCategory category, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND " +
           "b.condition = ?1")
    Page<Book> findByCondition(Book.BookCondition condition, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND " +
           "b.listingType = ?1")
    Page<Book> findByListingType(Book.ListingType listingType, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND " +
           "LOWER(b.location) LIKE LOWER(CONCAT('%', ?1, '%'))")
    Page<Book> findByLocation(String location, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', ?1, '%'))) AND " +
           "b.category = ?2 AND b.condition = ?3 AND b.listingType = ?4 AND " +
           "LOWER(b.location) LIKE LOWER(CONCAT('%', ?5, '%'))")
    Page<Book> searchBooksWithFilters(String keyword, Book.BookCategory category, 
                                     Book.BookCondition condition, Book.ListingType listingType, 
                                     String location, Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.createdAt >= ?1")
    Long countBooksCreatedAfter(LocalDateTime date);
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.status = ?1")
    Long countBooksByStatus(Book.BookStatus status);
}
