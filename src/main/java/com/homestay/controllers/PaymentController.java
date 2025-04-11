package com.homestay.controllers;

import com.homestay.models.Booking;
import com.homestay.models.Payment;
import com.homestay.services.BookingService;
import com.homestay.services.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;
    private final BookingService bookingService;

    // --- Thông tin tài khoản nhận tiền cố định ---
    private static final String BANK_BIN = "970436"; // Ví dụ: Vietcombank
    private static final String BANK_ACCOUNT_NUMBER = "1024888888"; // Số tài khoản nhận tiền
    private static final String BANK_ACCOUNT_NAME = "NGUYEN MINH SANG"; // Tên chủ TK (VIẾT HOA, KHÔNG DẤU)
    private static final String BANK_QUICKLINK_NAME = "Vietcombank"; // Tên ngân hàng để hiển thị
    // --------------------------------------------

    @Autowired
    public PaymentController(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    @ModelAttribute("bookingStatusEnum")
    public Class<Booking.BookingStatus> bookingStatusEnum() {
        return Booking.BookingStatus.class;
    }

    // Hiển thị lịch sử thanh toán cho booking
    @GetMapping("/history/{bookingId}")
    public String paymentHistoryForBooking(@PathVariable Long bookingId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Booking> bookingOpt = bookingService.getBookingById(bookingId);
        if (bookingOpt.isEmpty()) {
            logger.warn("Payment history requested for non-existent booking ID: {}", bookingId);
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn đặt phòng.");
            return "redirect:/user/booking"; // Về danh sách booking của user
        }

        List<Payment> payments = paymentService.getPaymentsForBooking(bookingOpt.get());
        model.addAttribute("payments", payments);
        model.addAttribute("booking", bookingOpt.get());
        logger.info("Displaying payment history for booking ID: {}", bookingId);
        return "user/Payment-history"; // Trả về đúng tên template Payment-history.html
    }

    // Trang quản lý thanh toán cho Admin
    @GetMapping("/manage")
    public String managePayments(Model model) {
        List<Payment> payments = paymentService.getAllPayments();
        model.addAttribute("payments", payments);
        logger.info("Admin accessed payment management page.");
        return "admin/Manage-Payment"; // Trả về đúng tên template Manage-Payment.html
    }

    // Xác nhận thanh toán thủ công (Admin)
    @PostMapping("/confirm/{id}")
    public String confirmManualPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Admin attempting to confirm payment ID: {}", id);
        Optional<Payment> paymentOpt = paymentService.getPaymentById(id);
        if (paymentOpt.isEmpty()) {
            logger.warn("Payment ID {} not found for confirmation.", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch thanh toán này.");
            return "redirect:/payment/manage";
        }

        Payment payment = paymentOpt.get();
        if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
            logger.warn("Admin cannot confirm payment ID {} because its status is {}.", id, payment.getStatus());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xác nhận thanh toán (trạng thái không phải PENDING).");
            return "redirect:/payment/manage";
        }

        try {
            boolean confirmed = paymentService.confirmManualPayment(id);
            if (confirmed) {
                logger.info("Admin successfully confirmed manual payment for ID: {}", id);
                redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thanh toán mã " + id + " thành công!");
            } else {
                logger.warn("Admin failed to confirm manual payment for ID: {}. confirmManualPayment returned false.", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xác nhận thanh toán (đã xảy ra lỗi không mong muốn).");
            }
        } catch (RuntimeException e) {
            logger.error("Error confirming payment ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xác nhận thanh toán: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error confirming payment ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống không xác định khi xác nhận thanh toán.");
        }
        return "redirect:/payment/manage";
    }

    // --- Endpoint hiển thị trang QR ---
    @GetMapping("/qr/{bookingId}")
    public String showQrPage(@PathVariable Long bookingId, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Request received for QR page for booking ID: {}", bookingId);
        Optional<Booking> bookingOpt = bookingService.getBookingById(bookingId);
        if (bookingOpt.isEmpty()) {
            logger.warn("QR page requested for non-existent booking ID: {}", bookingId);
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn đặt phòng.");
            return "redirect:/user/booking"; // Về danh sách booking
        }

        Booking booking = bookingOpt.get();

        // 2. Kiểm tra phương thức thanh toán
        if (!"QR_CODE".equalsIgnoreCase(booking.getPaymentMethod())) {
            logger.warn("Access denied to QR page for booking ID: {}. Payment method is not QR_CODE (it's {})", bookingId, booking.getPaymentMethod());
            redirectAttributes.addFlashAttribute("errorMessage", "Phương thức thanh toán của đơn này không phải là QR Code.");
            return "redirect:/payment/history/" + bookingId; // Về lịch sử để xem PTTT đúng
        }

        // 3. Kiểm tra trạng thái booking
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            logger.warn("Access denied to QR page for booking ID: {}. Booking status is not PENDING (it's {})", bookingId, booking.getStatus());
            String message = switch (booking.getStatus()) {
                case CONFIRMED -> "Đơn đặt phòng này đã được thanh toán và xác nhận.";
                case CANCELLED -> "Đơn đặt phòng này đã bị hủy.";
                case COMPLETED -> "Đơn đặt phòng này đã hoàn thành.";
                default -> "Trạng thái đơn đặt phòng không hợp lệ để thanh toán QR.";
            };
            redirectAttributes.addFlashAttribute("infoMessage", message);
            return "redirect:/payment/history/" + bookingId; // Về lịch sử
        }

        // 4. Kiểm tra giá trị tổng tiền
        if (booking.getTotalPrice() <= 0) {
            logger.error("Cannot generate QR for booking ID {} because total price is invalid: {}", bookingId, booking.getTotalPrice());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Đơn đặt phòng không có tổng tiền hợp lệ để tạo mã QR.");
            return "redirect:/payment/history/" + bookingId; // Về lịch sử
        }
        // --- Kết thúc kiểm tra ---

        try {
            // Tạo nội dung chuyển khoản (đảm bảo đủ thông tin và cố gắng duy nhất)
            String usernamePart = booking.getUser() != null ? booking.getUser().getUsername().replaceAll("[^a-zA-Z0-9]", "").toUpperCase() : "GUEST";
            String purpose = "TT BK" + booking.getId() + " " + usernamePart;
            purpose = purpose.substring(0, Math.min(purpose.length(), 50)); // Giới hạn 50 ký tự

            // Encode các thành phần URL
            String encodedPurpose = URLEncoder.encode(purpose, StandardCharsets.UTF_8.toString());
            String encodedAccountName = URLEncoder.encode(BANK_ACCOUNT_NAME, StandardCharsets.UTF_8.toString());

            // Tạo URL ảnh QR từ VietQR API
            String qrCodeImageUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s&accountName=%s",
                    BANK_BIN,
                    BANK_ACCOUNT_NUMBER,
                    (long) booking.getTotalPrice(), // Số tiền dạng long
                    encodedPurpose,
                    encodedAccountName
            );
            logger.info("Generated QR Code URL for booking ID {}: {}", bookingId, qrCodeImageUrl);

            // Đưa dữ liệu vào Model để hiển thị
            model.addAttribute("booking", booking);
            model.addAttribute("qrCodeImageUrl", qrCodeImageUrl);
            model.addAttribute("bankName", BANK_QUICKLINK_NAME);
            model.addAttribute("bankAccountNumber", BANK_ACCOUNT_NUMBER);
            model.addAttribute("bankAccountName", BANK_ACCOUNT_NAME); // Tên hiển thị trên web
            model.addAttribute("paymentAmount", booking.getTotalPrice());
            model.addAttribute("paymentContent", purpose); // Nội dung để copy

            return "user/Payment-QR"; // Trả về đúng tên template Payment-QR.html

        } catch (Exception e) {
            logger.error("Error generating QR content or URL for booking ID {}: {}", bookingId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi tạo mã QR. Vui lòng thử lại hoặc chọn phương thức khác.");
            return "redirect:/payment/history/" + bookingId; // Về lịch sử khi lỗi
        }
    }

    // Endpoint hiển thị trang thông tin thanh toán tĩnh (Nếu có)
    @GetMapping("/info")
    public String showPaymentInfo(Model model) {
        logger.info("Displaying static payment information page.");
        return "user/Payment"; // Trả về đúng tên template Payment.html
    }
}