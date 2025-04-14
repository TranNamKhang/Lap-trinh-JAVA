package com.homestay.services;

import com.homestay.models.Booking;
import com.homestay.models.Payment;
import com.homestay.repositories.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 
import java.math.BigDecimal;
import java.util.Collections; 
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService; 

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          @Lazy BookingService bookingService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    @Transactional 
    public Payment createInitialPayment(Booking booking, String paymentMethod, String transactionRef) {
        logger.info("Creating initial payment for booking ID: {}, Method: {}", booking.getId(), paymentMethod);
        if (booking.getTotalPrice() <= 0) {
            logger.error("Cannot create payment for booking ID {} due to zero or negative total price: {}", booking.getId(), booking.getTotalPrice());
            throw new IllegalArgumentException("Tổng tiền đặt phòng phải lớn hơn 0.");
        }
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(BigDecimal.valueOf(booking.getTotalPrice()));
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("PENDING"); 

        if (transactionRef != null && !transactionRef.isBlank()) {
            payment.setTransactionRef(transactionRef); // Ghi đè nếu được cung cấp
        }

        try {
            Payment savedPayment = paymentRepository.save(payment);
            logger.info("Initial payment record saved with ID: {} for booking ID: {}", savedPayment.getId(), booking.getId());
            return savedPayment;
        } catch (Exception e) {
            logger.error("!!! Error saving initial payment for booking ID {}: {}", booking.getId(), e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lưu thông tin thanh toán ban đầu.", e);
        }
    }

    public Optional<Payment> getPaymentById(Long id) {
        logger.debug("Fetching payment by ID: {}", id);
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getPaymentByTransactionRef(String transactionRef) {
        logger.debug("Fetching payment by transactionRef: {}", transactionRef);
        return paymentRepository.findByTransactionRef(transactionRef);
    }

    public List<Payment> getPaymentsForBooking(Booking booking) {
        if (booking == null || booking.getId() == null) {
            logger.warn("Attempted to get payments for a null or transient booking.");
            return Collections.emptyList(); 
        }
        logger.debug("Fetching payments for booking ID: {}", booking.getId());
        return paymentRepository.findByBooking(booking);
    }

    public List<Payment> getAllPayments() {
        logger.info("Fetching all payment records.");
        return paymentRepository.findAll();
    }

    @Transactional 
    public boolean confirmManualPayment(Long paymentId) {
        logger.info("Attempting manual confirmation for payment ID: {}", paymentId);
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            if ("PENDING".equals(payment.getStatus())) {
                try {
                    payment.setStatus("CONFIRMED"); 
                    paymentRepository.save(payment);
                    logger.info("Payment ID: {} status updated to CONFIRMED.", paymentId);

                    Booking booking = payment.getBooking();
                    if (booking == null) {
                        logger.error("CRITICAL: Booking is null for confirmed payment ID: {}", paymentId);
                        throw new IllegalStateException("Không tìm thấy thông tin đặt phòng liên quan đến thanh toán này.");
                    }

                    boolean bookingUpdated = bookingService.updateBookingStatus(booking.getId(), Booking.BookingStatus.CONFIRMED);
                    if (!bookingUpdated) {
                        logger.error("Failed to update booking status for booking ID: {} after confirming payment ID: {}", booking.getId(), paymentId);
                        throw new RuntimeException("Lỗi khi cập nhật trạng thái đặt phòng sau khi xác nhận thanh toán.");
                    }
                    logger.info("Booking ID: {} status updated to CONFIRMED following payment confirmation.", booking.getId());
                    return true; // Thành công

                } catch (Exception e) {
                    logger.error("!!! Error during manual confirmation process for payment ID {}: {}", paymentId, e.getMessage(), e);
                    throw new RuntimeException("Lỗi hệ thống khi xác nhận thanh toán.", e);
                }
            } else {
                logger.warn("Cannot confirm payment ID: {}. Status is already '{}'.", paymentId, payment.getStatus());
                return false; 
            }
        }
        logger.warn("Payment ID: {} not found for confirmation.", paymentId);
        return false; 
    }

   
}