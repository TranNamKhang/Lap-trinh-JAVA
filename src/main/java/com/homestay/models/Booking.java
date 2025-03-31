package com.homestay.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "homestay_id", nullable = false)
    private Homestay homestay;

    @NotNull(message = "Ngày nhận phòng không được null")
    private LocalDate checkIn;

    @NotNull(message = "Ngày trả phòng không được null")
    private LocalDate checkOut;

    @NotNull(message = "Tổng tiền không được null")
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDateTime bookingDate = LocalDateTime.now();

    private String paymentMethod;

    @Min(value = 1, message = "Số lượng khách phải lớn hơn 0")
    private int numberOfGuests;

    private boolean deleted;

    public boolean isActive() {
        return this.status == BookingStatus.CONFIRMED;
    }

    public void cancelBooking() {
        if (this.status == BookingStatus.CONFIRMED || this.status == BookingStatus.PENDING) {
            this.status = BookingStatus.CANCELLED;
        } else {
            throw new IllegalStateException("Không thể hủy đặt phòng đã hoàn thành hoặc đã bị hủy trước đó.");
        }
    }

    public enum BookingStatus {
        PENDING("Đang chờ xác nhận"),
        CONFIRMED("Đã xác nhận"),
        CANCELLED("Đã hủy"),
        COMPLETED("Hoàn thành");

        private final String description;
        BookingStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Homestay getHomestay() { return homestay; }
    public void setHomestay(Homestay homestay) { this.homestay = homestay; }

    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
