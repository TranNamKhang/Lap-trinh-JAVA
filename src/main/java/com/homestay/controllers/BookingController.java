package com.homestay.controllers;

import com.homestay.models.Booking;
import com.homestay.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Trang lịch sử đặt chỗ của người dùng
    @GetMapping("/history")
    public String userBookingHistory(Principal principal, Model model) {
        List<Booking> bookings = bookingService.getBookingsByUsername(principal.getName());
        model.addAttribute("bookings", bookings);
        return "booking-history";
    }

    // Trang quản lý booking của admin
    @GetMapping("/manage")
    public String manageBookings(Model model) {
        List<Booking> allBookings = bookingService.getAllBookings();
        model.addAttribute("bookings", allBookings);
        return "manage-booking";
    }

    // Chi tiết booking
    @GetMapping("/{id}/detail")
    public String viewBookingDetail(@PathVariable Long id, Model model) {
        Booking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));
        model.addAttribute("booking", booking);
        return "booking-detail";
    }

    // Cập nhật trạng thái booking (ví dụ từ admin)
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam Booking.BookingStatus status) {
        bookingService.updateBookingStatus(id, status);
        return "redirect:/booking/manage";
    }
}
