package com.bookbridge.repository;

import com.bookbridge.model.Book;
import com.bookbridge.model.CartItem;
import com.bookbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    
    Optional<CartItem> findByUserAndBook(User user, Book book);
    
    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.user = ?1")
    Long countCartItemsByUser(User user);
    
    @Query("SELECT SUM(c.quantity * b.price) FROM CartItem c JOIN c.book b WHERE c.user = ?1 AND b.listingType = 'SELL'")
    Double calculateCartTotal(User user);
    
    void deleteByUser(User user);
    
    void deleteByUserAndBook(User user, Book book);
}
