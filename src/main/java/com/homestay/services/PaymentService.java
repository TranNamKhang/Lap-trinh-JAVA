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

    // Lấy thông tin thanh toán theo ID
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    // Tạo thanh toán mới
    public Payment createPayment(Payment payment) {
        payment.setStatus("Pending"); // Mặc định trạng thái là "Pending"
        return paymentRepository.save(payment);
    }

    // Cập nhật trạng thái thanh toán
    public Payment updatePaymentStatus(Long id, String status) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setStatus(status);
            return paymentRepository.save(payment);
        }
        return null;
    }
}