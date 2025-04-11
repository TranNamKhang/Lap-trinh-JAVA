package com.homestay.services;

import com.homestay.models.Booking;
import com.homestay.models.Payment; // Import Payment nếu cần tham chiếu trực tiếp
import com.homestay.repositories.BookingRepository;
// import com.homestay.repositories.PaymentRepository; // Bỏ import nếu không dùng trực tiếp
import org.slf4j.Logger; // Thêm Logger
import org.slf4j.LoggerFactory; // Thêm Logger Factory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Đảm bảo có import này

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
// import java.util.stream.Collectors; // Bỏ import nếu không dùng stream trong này

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class); // Khởi tạo Logger
    private final BookingRepository bookingRepository;
    // private final PaymentRepository paymentRepository; // Có thể không cần trực tiếp ở đây nếu chỉ gọi PaymentService
    private final PaymentService paymentService; // Đảm bảo @Lazy nếu có dependency vòng tròn

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          // PaymentRepository paymentRepository, // Bỏ nếu không dùng
                          @Lazy PaymentService paymentService) { // Giữ @Lazy
        this.bookingRepository = bookingRepository;
        // this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    public List<Booking> getBookingsByUser(Long userId) {
        logger.debug("Fetching bookings for user ID: {}", userId);
        return bookingRepository.findByUserId(userId);
    }

    public Optional<Booking> getBookingById(Long id) {
        logger.debug("Fetching booking by ID: {}", id);
        return bookingRepository.findById(id);
    }

    // Tạo booking mới và gọi PaymentService để tạo payment ban đầu
    @Transactional // Quan trọng: Đảm bảo transaction bao gồm cả lưu booking và payment
    public Booking createBooking(Booking booking) {
        logger.info("Attempting to create booking for user ID: {}", booking.getUser() != null ? booking.getUser().getId() : "UNKNOWN");
        try {
            // Đặt trạng thái và ngày giờ ban đầu
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setBookingDate(LocalDateTime.now()); // Ghi nhận thời gian tạo booking

            // Lưu booking trước để lấy ID
            Booking savedBooking = bookingRepository.save(booking);
            logger.info("Booking entity saved with ID: {}", savedBooking.getId());

            // Gọi PaymentService để tạo bản ghi thanh toán ban đầu
            // Chỉ tạo nếu có phương thức thanh toán và tổng tiền > 0
            if (savedBooking.getPaymentMethod() != null && !savedBooking.getPaymentMethod().isEmpty() && savedBooking.getTotalPrice() > 0) {
                logger.debug("Creating initial payment record for booking ID: {}", savedBooking.getId());
                // PaymentService sẽ tự xử lý việc lưu Payment và liên kết với Booking
                Payment initialPayment = paymentService.createInitialPayment(savedBooking, savedBooking.getPaymentMethod(), null); // transactionRef để null tự sinh
                logger.info("Initial payment record created with ID: {} and status: {}", initialPayment.getId(), initialPayment.getStatus());
                // Không cần add lại payment vào booking list ở đây vì PaymentService đã làm (hoặc cascade persist)
            } else {
                logger.warn("Skipping initial payment creation for booking ID: {}. Reason: Missing payment method or zero total price.", savedBooking.getId());
            }

            return savedBooking;
        } catch (Exception e) {
            // Log lỗi chi tiết nếu có vấn đề khi lưu booking hoặc tạo payment ban đầu
            logger.error("!!! Error during booking creation persistence for user {}: {}", booking.getUser() != null ? booking.getUser().getUsername() : "UNKNOWN", e.getMessage(), e);
            // Ném lại lỗi để Controller có thể bắt và xử lý (hoặc để transaction rollback)
            throw new RuntimeException("Lỗi khi lưu thông tin đặt phòng hoặc tạo thanh toán.", e);
        }
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        logger.warn("Attempting to cancel booking ID: {}", bookingId);
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            // Chỉ cho phép hủy nếu đang PENDING hoặc CONFIRMED
            if (booking.getStatus() == Booking.BookingStatus.PENDING || booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
                booking.setStatus(Booking.BookingStatus.CANCELLED); // Đặt trạng thái hủy
                bookingRepository.save(booking); // Lưu lại
                logger.info("Booking ID: {} status updated to CANCELLED.", bookingId);

                // Gọi PaymentService để xử lý hủy các payment liên quan (nếu cần)
                // Ví dụ: paymentService.cancelPendingPaymentsForBooking(booking);
                // Hiện tại PaymentService chưa có hàm này, logic hủy payment đã được tích hợp trong cancelBooking của controller hoặc confirmManualPayment của PaymentService
            } else {
                logger.warn("Cannot cancel booking ID: {} because its status is {}", bookingId, booking.getStatus());
                throw new IllegalStateException("Không thể hủy đặt phòng ở trạng thái " + booking.getStatus().getDescription() + ".");
            }
        } else {
            logger.warn("Booking ID: {} not found for cancellation.", bookingId);
            // Có thể ném lỗi hoặc không tùy logic mong muốn
        }
    }

    // Giữ nguyên deleteBookingsByHomestayId và updateBookingStatus nếu có

    // @Transactional
    // public void deleteBookingsByHomestayId(Long homestayId) { ... }

    @Transactional
    public boolean updateBookingStatus(Long id, Booking.BookingStatus bookingStatus) {
        logger.info("Attempting to update status of booking ID: {} to {}", id, bookingStatus);
        Optional<Booking> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            booking.setStatus(bookingStatus);
            bookingRepository.save(booking);
            logger.info("Booking ID: {} status successfully updated to {}", id, bookingStatus);
            return true;
        }
        logger.warn("Booking ID: {} not found for status update.", id);
        return false;
    }

}