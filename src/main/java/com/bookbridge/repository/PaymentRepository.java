package com.bookbridge.repository;

import com.bookbridge.model.Order;
import com.bookbridge.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);
    
    Optional<Payment> findByOrder(Order order);
    
    Optional<Payment> findByEsewaTransactionId(String esewaTransactionId);
    
    @Query("SELECT p FROM Payment p WHERE p.status = ?1")
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = ?1")
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    @Query("SELECT p FROM Payment p ORDER BY p.createdAt DESC")
    Page<Payment> findAllPaymentsPaged(Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = ?1")
    Long countPaymentsByStatus(Payment.PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.createdAt >= ?1")
    Long countPaymentsCreatedAfter(LocalDateTime date);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN ?1 AND ?2")
    Double sumSuccessfulPaymentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
}
