package com.homestay.controllers;

import com.homestay.models.Booking;
import com.homestay.models.Homestay;
import com.homestay.models.User;
import com.homestay.services.BookingService;
import com.homestay.services.HomestayService;
import com.homestay.services.UserService;

import org.springframework.security.core.Authentication;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequestMapping("/user/booking")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private final BookingService bookingService;
    private final HomestayService homestayService;
    private final UserService userService;

    @Autowired
    public BookingController(BookingService bookingService, HomestayService homestayService, UserService userService) {
        this.bookingService = bookingService;
        this.homestayService = homestayService;
        this.userService = userService;
    }

    @GetMapping("/form")
    public String showBookingForm(@RequestParam("homestayId") Long homestayId,
                                  @AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.getUsername(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            logger.warn("User not found for username: {}", userDetails.getUsername());
            return "redirect:/auth/login?error=userNotFound";
        }
        User user = userOptional.get();

        Optional<Homestay> homestayOptional = homestayService.getHomestayById(homestayId);
        if (homestayOptional.isEmpty()) {
            logger.warn("Homestay not found for ID: {}", homestayId);
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy Homestay với ID " + homestayId);
            return "redirect:/user/home";
        }
        Homestay homestay = homestayOptional.get();

        if (homestay.getPricePerNight() == null || homestay.getPricePerNight().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Homestay ID {} has invalid price: {}", homestayId, homestay.getPricePerNight());
            redirectAttributes.addFlashAttribute("errorMessage", "Homestay này hiện không có giá hoặc giá không hợp lệ, không thể đặt phòng.");
            return "redirect:/user/homestays/" + homestayId;
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setHomestay(homestay);
        booking.setNumberOfGuests(1);
        booking.setTotalPrice(0);

        model.addAttribute("booking", booking);
        model.addAttribute("homestay", homestay);
        logger.info("Showing booking form for homestay ID: {} for user ID: {}", homestayId, user.getId());
        return "user/booking/form";
    }


    @PostMapping
    public String createBooking(@Valid @ModelAttribute("booking") Booking booking,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        String username = userDetails.getUsername();
        logger.info("Processing booking submission for user: {}", username);
        Homestay homestay = null;

        Optional<User> userOptional = userService.getUsername(username);
        if (userOptional.isEmpty()) {
            logger.error("CRITICAL: Authenticated user '{}' not found in database.", username);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: Không tìm thấy thông tin người dùng.");
            return "redirect:/error";
        }
        User user = userOptional.get();
        booking.setUser(user);
        logger.debug("User set for booking: {}", user.getId());

        booking.setAppointmentDate(LocalDate.now());
        logger.debug("Set appointmentDate to now: {}", booking.getAppointmentDate());

        if (booking.getHomestay() == null || booking.getHomestay().getId() == null) {
            logger.error("Validation Error: Homestay ID is missing in the submitted booking object for user {}", username);
            model.addAttribute("booking", booking); // Giữ lại dữ liệu form
            model.addAttribute("error", "Lỗi hệ thống: Thiếu thông tin Homestay."); // Thêm lỗi chung
            return "user/booking/form"; // Quay lại form
        }
        Long homestayId = booking.getHomestay().getId();
        Optional<Homestay> homestayOptional = homestayService.getHomestayById(homestayId);
        if (homestayOptional.isEmpty()) {
            logger.error("Validation Error: Homestay with ID {} not found for booking by user {}", homestayId, username);
            model.addAttribute("booking", booking);
            model.addAttribute("error", "Homestay bạn chọn không còn tồn tại."); // Thêm lỗi chung
            return "user/booking/form";
        }
        homestay = homestayOptional.get();
        booking.setHomestay(homestay);
        logger.debug("Homestay found and set for booking: {}", homestay.getId());


        try {
            if (booking.getCheckIn() != null && booking.getCheckOut() != null) {
                LocalDate today = LocalDate.now();
                if (booking.getCheckIn().isBefore(today)) {
                    bindingResult.rejectValue("checkIn", "FutureOrPresent.booking.checkIn", "Ngày nhận phòng không được trước ngày hôm nay.");
                }
                if (!booking.getCheckOut().isAfter(booking.getCheckIn())) {
                    bindingResult.rejectValue("checkOut", "After.booking.checkOut", "Ngày trả phòng phải sau ngày nhận phòng.");
                }
            }

            if (bindingResult.hasErrors()) {
                logger.warn("Booking form validation failed for user {}:", username);
                for (FieldError error : bindingResult.getFieldErrors()) {
                    logger.warn("Field '{}': Error - '{}', Rejected Value: '{}'", error.getField(), error.getDefaultMessage(), error.getRejectedValue());
                }
                model.addAttribute("homestay", homestay);
                model.addAttribute("booking", booking);
                return "user/booking/form";
            }
            logger.debug("Form validation passed for user: {}", username);

            logger.debug("Calculating total price server-side...");
            if (homestay.getPricePerNight() != null && homestay.getPricePerNight().compareTo(BigDecimal.ZERO) > 0) {
                long nights = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
                if (nights > 0) {
                    BigDecimal total = homestay.getPricePerNight().multiply(BigDecimal.valueOf(nights));
                    booking.setTotalPrice(total.doubleValue());
                    logger.info("Server calculated total price: {} for {} nights", booking.getTotalPrice(), nights);
                } else {
                    logger.error("Error calculating nights server-side (nights = {}). CheckIn: {}, CheckOut: {}", nights, booking.getCheckIn(), booking.getCheckOut());
                    throw new IllegalStateException("Ngày trả phòng không hợp lệ.");
                }
            } else {
                logger.error("Cannot calculate price: Homestay price is invalid ({}).", homestay.getPricePerNight());
                throw new IllegalStateException("Giá Homestay không hợp lệ.");
            }
            logger.debug("Total price set.");

            logger.debug("Calling bookingService.createBooking...");
            Booking savedBooking = bookingService.createBooking(booking);
            logger.info("Booking saved successfully with ID: {} for user ID: {}", savedBooking.getId(), user.getId());

            if ("QR_CODE".equals(savedBooking.getPaymentMethod())) {
                logger.info("Redirecting user {} to QR payment page for booking ID: {}", username, savedBooking.getId());
                return "redirect:/payment/qr/" + savedBooking.getId();
            } else {
                logger.info("Redirecting user {} to payment history page for booking ID: {}", username, savedBooking.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Đặt phòng thành công! Vui lòng kiểm tra thông tin và trạng thái thanh toán.");
                return "redirect:/payment/history/" + savedBooking.getId();
            }

        } catch (Exception e) {
            logger.error("!!! Exception occurred during booking process for user {}: {}", username, e.getMessage(), e);
            model.addAttribute("error", "Đã xảy ra lỗi không mong muốn khi đặt phòng. Vui lòng thử lại.");
            if (homestay != null) {
                model.addAttribute("homestay", homestay);
            }
            model.addAttribute("booking", booking);
            return "user/booking/form";
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.getUsername(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            logger.warn("User not found for username during cancel attempt: {}", userDetails.getUsername());
            return "redirect:/auth/login?error=userNotFound";
        }
        User user = userOptional.get();

        Optional<Booking> bookingOptional = bookingService.getBookingById(id);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();

            if (booking.getUser() != null && booking.getUser().getId().equals(user.getId())) {
                try {
                    bookingService.cancelBooking(id);
                    redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đặt phòng mã " + id + " thành công.");
                    logger.info("Booking ID: {} cancelled successfully by user ID: {}", id, user.getId());
                } catch (IllegalStateException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đặt phòng: " + e.getMessage());
                    logger.warn("Failed to cancel booking ID: {} by user ID: {}. Reason: {}", id, user.getId(), e.getMessage());
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi hủy đặt phòng.");
                    logger.error("System error cancelling booking ID: {} by user ID: {}", id, user.getId(), e);
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền hủy đặt phòng này.");
                logger.warn("Unauthorized cancel attempt for booking ID: {} by user ID: {}", id, user.getId());
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đặt phòng mã " + id + ".");
            logger.warn("Booking not found for cancel attempt with ID: {}", id);
        }
        return "redirect:/user/booking";
    }

    @GetMapping("/my-bookings")
public String myBookings(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
        return "redirect:/auth/login";
    }

    String username = authentication.getName();
    Optional<User> userOptional = userService.getUsername(username);
    
    if (userOptional.isEmpty()) {
        return "redirect:/auth/login";
    }
    
    User user = userOptional.get();
    Long userId = user.getId();
    
    List<Booking> allBookings = bookingService.getBookingsByUser(userId);
    
    List<Booking> pendingBookings = new ArrayList<>();
    List<Booking> confirmedBookings = new ArrayList<>();
    List<Booking> completedBookings = new ArrayList<>();
    List<Booking> cancelledBookings = new ArrayList<>();
    
    for (Booking booking : allBookings) {
        switch (booking.getStatus()) {
            case PENDING:
                pendingBookings.add(booking);
                break;
            case CONFIRMED:
                confirmedBookings.add(booking);
                break;
            case COMPLETED:
                completedBookings.add(booking);
                break;
            case CANCELLED:
                cancelledBookings.add(booking);
                break;
        }
    }
    
    model.addAttribute("bookings", allBookings);
    model.addAttribute("pendingBookings", pendingBookings);
    model.addAttribute("confirmedBookings", confirmedBookings);
    model.addAttribute("completedBookings", completedBookings);
    model.addAttribute("cancelledBookings", cancelledBookings);
    
    return "user/booking/my-bookings";
}

@PostMapping("/booking/cancel")
public String cancelBooking(@RequestParam("bookingId") Long bookingId, RedirectAttributes redirectAttributes) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
        return "redirect:/auth/login";
    }
    
    try {
        Optional<Booking> bookingOptional = bookingService.getBookingById(bookingId);
        if (bookingOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn đặt phòng!");
            return "redirect:/user/booking/my-bookings";
        }
        
        Booking booking = bookingOptional.get();
        String username = authentication.getName();
        if (!booking.getUser().getUsername().equals(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền hủy đơn đặt phòng này!");
            return "redirect:/user/booking/my-bookings";
        }
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể hủy đơn đặt phòng đang chờ xác nhận!");
            return "redirect:/user/booking/my-bookings";
        }
        
        bookingService.cancelBooking(bookingId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đặt phòng thành công!");
        
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hủy đơn đặt phòng: " + e.getMessage());
    }
    
    return "redirect:/user/booking/my-bookings";
}

}