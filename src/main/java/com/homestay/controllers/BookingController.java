package com.homestay.controllers;

import com.homestay.models.Booking;
import com.homestay.models.Homestay;
import com.homestay.models.User;
import com.homestay.services.BookingService;
import com.homestay.services.HomestayService;
import com.homestay.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/user/booking")
public class BookingController {

    private final BookingService bookingService;
    private final HomestayService homestayService;
    private final UserService userService;

    public BookingController(BookingService bookingService, HomestayService homestayService, UserService userService) {
        this.bookingService = bookingService;
        this.homestayService = homestayService;
        this.userService = userService;
    }

    @GetMapping
    public String getUserBookings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> userOptional = userService.getUsername(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOptional.get();
        model.addAttribute("bookings", bookingService.getBookingsByUser(user.getId()));
        return "user/booking/list";
    }

    @GetMapping("/form")
    public String showBookingForm(@RequestParam("homestayId") Long homestayId,
                                  @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> userOptional = userService.getUsername(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOptional.get();
        
        Optional<Homestay> homestay = homestayService.getHomestayById(homestayId);
        if (homestay.isEmpty()) {
            return "error/404";
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setHomestay(homestay.get());

        model.addAttribute("booking", booking);
        model.addAttribute("homestay", homestay.get());
        return "user/booking/form";
    }

    @PostMapping
    public String createBooking(@ModelAttribute Booking booking,
                                @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOptional = userService.getUsername(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOptional.get();
        booking.setUser(user);
        bookingService.createBooking(booking);
        return "redirect:/user/booking";
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOptional = userService.getUsername(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOptional.get();

        Optional<Booking> bookingOptional = bookingService.getBookingById(id);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            
            if (booking.getUser() != null && booking.getUser().getId().equals(user.getId())) {
                bookingService.cancelBooking(id);
            }
        }
        return "redirect:/user/booking";
    }
}