package com.homestay.services;

import com.homestay.models.Booking;
import com.homestay.models.Payment;
import com.homestay.repositories.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Đảm bảo import đúng

import java.math.BigDecimal;
import java.util.Collections; // Import Collections
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService; // Giữ @Lazy nếu cần

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          @Lazy BookingService bookingService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    // Tạo bản ghi Payment mới
    @Transactional // Quan trọng: Đảm bảo là transactional
    public Payment createInitialPayment(Booking booking, String paymentMethod, String transactionRef) {
        logger.info("Creating initial payment for booking ID: {}, Method: {}", booking.getId(), paymentMethod);
        if (booking.getTotalPrice() <= 0) {
            logger.error("Cannot create payment for booking ID {} due to zero or negative total price: {}", booking.getId(), booking.getTotalPrice());
            // Có thể ném lỗi để dừng quá trình booking nếu tổng tiền <= 0 là không hợp lệ
            throw new IllegalArgumentException("Tổng tiền đặt phòng phải lớn hơn 0.");
        }
        Payment payment = new Payment();
        payment.setBooking(booking);
        // Lưu số tiền bằng BigDecimal để chính xác
        payment.setAmount(BigDecimal.valueOf(booking.getTotalPrice()));
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("PENDING"); // Trạng thái ban đầu luôn là PENDING

        // Tự động sinh transactionRef nếu không được cung cấp (logic này đã có trong @PrePersist của Payment)
        if (transactionRef != null && !transactionRef.isBlank()) {
            payment.setTransactionRef(transactionRef); // Ghi đè nếu được cung cấp
        }

        try {
            Payment savedPayment = paymentRepository.save(payment);
            logger.info("Initial payment record saved with ID: {} for booking ID: {}", savedPayment.getId(), booking.getId());
            // Không cần add lại vào booking.getPayments() nếu dùng cascade hoặc service quản lý
            return savedPayment;
        } catch (Exception e) {
            logger.error("!!! Error saving initial payment for booking ID {}: {}", booking.getId(), e.getMessage(), e);
            // Ném lại lỗi để transaction có thể rollback
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
            return Collections.emptyList(); // Trả về danh sách rỗng
        }
        logger.debug("Fetching payments for booking ID: {}", booking.getId());
        return paymentRepository.findByBooking(booking);
    }

    public List<Payment> getAllPayments() {
        logger.info("Fetching all payment records.");
        return paymentRepository.findAll();
    }

    // Xác nhận thanh toán thủ công (cho CASH, QR, BANK_TRANSFER)
    @Transactional // Rất quan trọng
    public boolean confirmManualPayment(Long paymentId) {
        logger.info("Attempting manual confirmation for payment ID: {}", paymentId);
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            // Chỉ xác nhận nếu đang PENDING
            if ("PENDING".equals(payment.getStatus())) {
                try {
                    payment.setStatus("CONFIRMED"); // Cập nhật trạng thái Payment
                    paymentRepository.save(payment);
                    logger.info("Payment ID: {} status updated to CONFIRMED.", paymentId);

                    // Lấy Booking liên quan
                    Booking booking = payment.getBooking();
                    if (booking == null) {
                        // Trường hợp hiếm gặp nhưng cần xử lý
                        logger.error("CRITICAL: Booking is null for confirmed payment ID: {}", paymentId);
                        throw new IllegalStateException("Không tìm thấy thông tin đặt phòng liên quan đến thanh toán này.");
                    }

                    // Gọi BookingService để cập nhật trạng thái Booking
                    boolean bookingUpdated = bookingService.updateBookingStatus(booking.getId(), Booking.BookingStatus.CONFIRMED);
                    if (!bookingUpdated) {
                        // Nếu không cập nhật được booking status -> lỗi -> rollback transaction
                        logger.error("Failed to update booking status for booking ID: {} after confirming payment ID: {}", booking.getId(), paymentId);
                        throw new RuntimeException("Lỗi khi cập nhật trạng thái đặt phòng sau khi xác nhận thanh toán.");
                    }
                    logger.info("Booking ID: {} status updated to CONFIRMED following payment confirmation.", booking.getId());
                    return true; // Thành công

                } catch (Exception e) {
                    // Bắt lỗi trong quá trình lưu hoặc cập nhật booking status
                    logger.error("!!! Error during manual confirmation process for payment ID {}: {}", paymentId, e.getMessage(), e);
                    // Ném lại lỗi để rollback transaction
                    throw new RuntimeException("Lỗi hệ thống khi xác nhận thanh toán.", e);
                }
            } else {
                logger.warn("Cannot confirm payment ID: {}. Status is already '{}'.", paymentId, payment.getStatus());
                return false; // Không thể xác nhận vì trạng thái không phải PENDING
            }
        }
        logger.warn("Payment ID: {} not found for confirmation.", paymentId);
        return false; // Không tìm thấy payment
    }

    // Có thể thêm hàm hủy payment nếu cần
     /*
     @Transactional
     public boolean cancelPayment(Long paymentId) {
        // Logic tương tự confirm nhưng đặt status là CANCELLED
        // Lưu ý: Thường không cần cập nhật trạng thái booking khi hủy payment, trừ khi đó là payment duy nhất và booking cũng cần hủy.
     }
     */
}