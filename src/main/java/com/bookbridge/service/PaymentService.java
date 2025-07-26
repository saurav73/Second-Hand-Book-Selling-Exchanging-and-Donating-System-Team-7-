package com.bookbridge.service;

import com.bookbridge.model.Order;
import com.bookbridge.model.Payment;
import com.bookbridge.repository.OrderRepository;
import com.bookbridge.repository.PaymentRepository;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Value("${esewa.merchant.code}")
    private String merchantCode;
    
    @Value("${esewa.base.url}")
    private String esewaBaseUrl;
    
    @Value("${esewa.success.url}")
    private String successUrl;
    
    @Value("${esewa.failure.url}")
    private String failureUrl;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Page<Payment> getAllPaymentsPaged(Pageable pageable) {
        return paymentRepository.findAllPaymentsPaged(pageable);
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getPaymentByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId);
    }

    public Optional<Payment> getPaymentByOrder(Order order) {
        return paymentRepository.findByOrder(order);
    }

    @Transactional
    public Payment initiateEsewaPayment(Order order) {
        // Create payment record
        Payment payment = new Payment(order, order.getTotalAmount(), Payment.PaymentMethod.ESEWA);
        payment.setMerchantCode(merchantCode);
        payment.setSuccessUrl(successUrl);
        payment.setFailureUrl(failureUrl);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        return savedPayment;
    }

    public Map<String, String> getEsewaPaymentParams(Payment payment) {
        Map<String, String> params = new HashMap<>();
        params.put("amt", payment.getAmount().toString());
        params.put("pdc", "0");
        params.put("psc", "0");
        params.put("txAmt", "0");
        params.put("tAmt", payment.getAmount().toString());
        params.put("pid", payment.getPaymentId());
        params.put("scd", merchantCode);
        params.put("su", successUrl + "?pid=" + payment.getPaymentId());
        params.put("fu", failureUrl + "?pid=" + payment.getPaymentId());
        
        return params;
    }

    @Transactional
    public Payment verifyEsewaPayment(String paymentId, String refId) {
        Optional<Payment> paymentOpt = paymentRepository.findByPaymentId(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            
            try {
                boolean verified = verifyWithEsewa(merchantCode, refId, payment.getAmount().toString(), paymentId);
                
                if (verified) {
                    payment.setStatus(Payment.PaymentStatus.SUCCESS);
                    payment.setEsewaRefId(refId);
                    payment.setCompletedAt(LocalDateTime.now());
                    
                    // Update order status
                    Order order = payment.getOrder();
                    order.setStatus(Order.OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                } else {
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    payment.setFailureReason("Payment verification failed");
                }
            } catch (Exception e) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason("Error during verification: " + e.getMessage());
            }
            
            return paymentRepository.save(payment);
        }
        throw new IllegalArgumentException("Payment not found");
    }

    private boolean verifyWithEsewa(String merchantCode, String refId, String amount, String paymentId) throws IOException {
        String verifyUrl = esewaBaseUrl + "/epay/transrec";
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(verifyUrl);
            
            String requestBody = "scd=" + merchantCode + "&rid=" + refId + "&amt=" + amount + "&pid=" + paymentId;
            StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_FORM_URLENCODED);
            httpPost.setEntity(entity);
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                return statusCode == 200;
            }
        }
    }

    @Transactional
    public Payment updatePaymentStatus(Long paymentId, Payment.PaymentStatus status) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(status);
            
            if (status == Payment.PaymentStatus.SUCCESS) {
                payment.setCompletedAt(LocalDateTime.now());
                
                // Update order status
                Order order = payment.getOrder();
                order.setStatus(Order.OrderStatus.CONFIRMED);
                orderRepository.save(order);
            }
            
            return paymentRepository.save(payment);
        }
        throw new IllegalArgumentException("Payment not found");
    }

    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public List<Payment> getPaymentsByMethod(Payment.PaymentMethod method) {
        return paymentRepository.findByPaymentMethod(method);
    }

    public Long countPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.countPaymentsByStatus(status);
    }

    public Long countPaymentsCreatedAfter(LocalDateTime date) {
        return paymentRepository.countPaymentsCreatedAfter(date);
    }

    public Double sumSuccessfulPaymentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        Double sum = paymentRepository.sumSuccessfulPaymentsBetweenDates(startDate, endDate);
        return sum != null ? sum : 0.0;
    }
}
