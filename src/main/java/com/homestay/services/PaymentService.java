package com.homestay.services;

import com.homestay.models.Payment;
import com.homestay.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }
}
