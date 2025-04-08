package com.homestay.services;

import com.homestay.models.Booking;
import com.homestay.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    // Lấy danh sách tất cả các booking
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Lấy booking theo ID
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    // Tạo booking mới
    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    // Cập nhật booking
    public Booking updateBooking(Long id, Booking bookingDetails) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setCheckIn(bookingDetails.getCheckIn());
            booking.setCheckOut(bookingDetails.getCheckOut());
            booking.setTotalPrice(bookingDetails.getTotalPrice());
            booking.setStatus(bookingDetails.getStatus());
            booking.setPaymentMethod(bookingDetails.getPaymentMethod());
            return bookingRepository.save(booking);
        }).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    // Xóa booking
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    // Cập nhật trạng thái booking
    public Booking updateBookingStatus(Long id, Booking.BookingStatus status) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus(status);
            return bookingRepository.save(booking);
        }).orElseThrow(() -> new RuntimeException("Booking not found"));
    }
}
