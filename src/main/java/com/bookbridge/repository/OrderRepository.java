package com.bookbridge.repository;

import com.bookbridge.model.Order;
import com.bookbridge.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    @Query("SELECT o FROM Order o WHERE o.status = ?1")
    List<Order> findByStatus(Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.deliveryStatus = ?1")
    List<Order> findByDeliveryStatus(Order.DeliveryStatus deliveryStatus);
    
    @Query("SELECT o FROM Order o WHERE o.user = ?1 ORDER BY o.createdAt DESC")
    Page<Order> findUserOrdersPaged(User user, Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= ?1")
    Long countOrdersCreatedAfter(LocalDateTime date);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = ?1")
    Long countOrdersByStatus(Order.OrderStatus status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN ?1 AND ?2")
    Double sumOrderAmountBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
}
