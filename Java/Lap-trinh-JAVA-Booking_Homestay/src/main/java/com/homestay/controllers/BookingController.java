package com.homestay.controllers;

import com.homestay.models.Booking;
import com.homestay.models.Booking.BookingStatus;
import com.homestay.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Lấy tất cả booking
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // Lấy booking theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo mới booking (qua JSON)
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Booking savedBooking = bookingService.createBooking(booking);
        return ResponseEntity.ok(savedBooking);
    }

    // Cập nhật booking
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking bookingDetails) {
        Booking updatedBooking = bookingService.updateBooking(id, bookingDetails);
        return ResponseEntity.ok(updatedBooking);
    }

    // Xóa booking
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    // Cập nhật trạng thái booking
    @PatchMapping("/{id}/status")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Long id, @RequestParam BookingStatus status) {
        Booking updatedBooking = bookingService.updateBookingStatus(id, status);
        return ResponseEntity.ok(updatedBooking);
    }
}