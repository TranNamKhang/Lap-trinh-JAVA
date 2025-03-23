package com.homestay.services;

import com.homestay.models.Payment;
import com.homestay.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // Lấy danh sách tất cả thanh toán
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }


}