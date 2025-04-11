package com.homestay.controllers;

import com.homestay.dto.BookingForm;
import com.homestay.models.Booking;
import com.homestay.models.Homestay;
import com.homestay.models.User;
import com.homestay.repositories.HomestayRepository;
import com.homestay.repositories.UserRepository;
import com.homestay.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/booking")
public class BookingFormController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @PostMapping("/submit")
    public String submitBooking(@ModelAttribute BookingForm bookingForm, Principal principal, Model model) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Homestay homestay = homestayRepository.findById(bookingForm.getHomestayId())
                .orElseThrow(() -> new RuntimeException("Homestay not found"));

        long days = ChronoUnit.DAYS.between(bookingForm.getCheckIn(), bookingForm.getCheckOut());
        double totalPrice = homestay.getPricePerNight() * days;

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setHomestay(homestay);
        booking.setCheckIn(bookingForm.getCheckIn());
        booking.setCheckOut(bookingForm.getCheckOut());
        booking.setNumberOfGuests(bookingForm.getNumberOfGuests());
        booking.setPaymentMethod(bookingForm.getPaymentMethod());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(Booking.BookingStatus.PENDING);

        bookingService.createBooking(booking);

        model.addAttribute("message", "Đặt chỗ thành công!");
        return "redirect:/booking/history";
    }
}
