package com.homestay.services;

import com.homestay.models.Booking;
import com.homestay.repositories.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Lấy danh sách đặt phòng của người dùng
    public List<Booking> getBookingsByUser (Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    // Lấy thông tin đặt phòng theo ID
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    // Tạo đặt phòng mới
    public Booking createBooking(Booking booking) {
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setBookingDate(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    // Hủy đặt phòng
    public void cancelBooking(Long bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            booking.cancelBooking(); // Gọi phương thức hủy đặt phòng
            bookingRepository.save(booking); // Lưu lại trạng thái đã hủy
        }
    }

    // Xóa tất cả các booking liên quan đến một homestay
    public void deleteBookingsByHomestayId(Long homestayId) {
        List<Booking> bookings = bookingRepository.findByHomestayId(homestayId);
        for (Booking booking : bookings) {
            bookingRepository.delete(booking);
        }
    }
}