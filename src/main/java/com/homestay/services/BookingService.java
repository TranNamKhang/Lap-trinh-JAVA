package com.homestay.services;

import com.homestay.models.Booking;
import com.homestay.models.Payment;
import com.homestay.repositories.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    private final TicketService ticketService;
    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          @Lazy PaymentService paymentService,
                          @Lazy TicketService ticketService) {
        this.bookingRepository = bookingRepository;
        this.paymentService = paymentService;
        this.ticketService = ticketService;
    }

    public List<Booking> getBookingsByUser(Long userId) {
        logger.debug("Fetching bookings for user ID: {}", userId);
        return bookingRepository.findByUserId(userId);
    }

    public Optional<Booking> getBookingById(Long id) {
        logger.debug("Fetching booking by ID: {}", id);
        return bookingRepository.findById(id);
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        logger.info("Attempting to create booking for user ID: {}", booking.getUser() != null ? booking.getUser().getId() : "UNKNOWN");
        try {
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setBookingDate(LocalDateTime.now());

            Booking savedBooking = bookingRepository.save(booking);
            logger.info("Booking entity saved with ID: {}", savedBooking.getId());

            if (savedBooking.getPaymentMethod() != null && !savedBooking.getPaymentMethod().isEmpty() && savedBooking.getTotalPrice() > 0) {
                logger.debug("Creating initial payment record for booking ID: {}", savedBooking.getId());
                Payment initialPayment = paymentService.createInitialPayment(savedBooking, savedBooking.getPaymentMethod(), null);
                logger.info("Initial payment record created with ID: {} and status: {}", initialPayment.getId(), initialPayment.getStatus());
            } else {
                logger.warn("Skipping initial payment creation for booking ID: {}. Reason: Missing payment method or zero total price.", savedBooking.getId());
            }

            return savedBooking;
        } catch (Exception e) {
            logger.error("!!! Error during booking creation persistence for user {}: {}", booking.getUser() != null ? booking.getUser().getUsername() : "UNKNOWN", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lưu thông tin đặt phòng hoặc tạo thanh toán.", e);
        }
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        logger.warn("Attempting to cancel booking ID: {}", bookingId);
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            if (booking.getStatus() == Booking.BookingStatus.PENDING || booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                logger.info("Booking ID: {} status updated to CANCELLED.", bookingId);

            } else {
                logger.warn("Cannot cancel booking ID: {} because its status is {}", bookingId, booking.getStatus());
                throw new IllegalStateException("Không thể hủy đặt phòng ở trạng thái " + booking.getStatus().getDescription() + ".");
            }
        } else {
            logger.warn("Booking ID: {} not found for cancellation.", bookingId);
        }
    }

    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    public double getTotalRevenue() {
        List<Booking> confirmedBookings = bookingRepository.findByStatus(Booking.BookingStatus.CONFIRMED);
        return confirmedBookings.stream()
                .mapToDouble(Booking::getTotalPrice)
                .sum();
    }

    @Transactional
    public boolean updateBookingStatus(Long id, Booking.BookingStatus bookingStatus) {
        logger.info("Attempting to update status of booking ID: {} to {}", id, bookingStatus);
        Optional<Booking> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            booking.setStatus(bookingStatus);
            bookingRepository.save(booking);

            if (bookingStatus == Booking.BookingStatus.CONFIRMED) {
                ticketService.generateTicket(booking);
                logger.info("Ticket generated for confirmed booking ID: {}", id);
            }

            logger.info("Booking ID: {} status successfully updated to {}", id, bookingStatus);
            return true;
        }
        logger.warn("Booking ID: {} not found for status update.", id);
        return false;
    }
}
