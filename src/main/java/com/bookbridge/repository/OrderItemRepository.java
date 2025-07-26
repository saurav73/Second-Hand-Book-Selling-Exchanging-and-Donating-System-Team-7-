package com.bookbridge.repository;

import com.bookbridge.model.Book;
import com.bookbridge.model.Order;
import com.bookbridge.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    
    List<OrderItem> findByBook(Book book);
    
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.book = ?1")
    Long countOrderItemsByBook(Book book);
}
