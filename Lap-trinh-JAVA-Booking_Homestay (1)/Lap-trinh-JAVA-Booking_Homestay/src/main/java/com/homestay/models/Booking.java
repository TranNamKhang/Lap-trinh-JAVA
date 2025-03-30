package com.homestay.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private BookingStatus status;

    private LocalDateTime bookingDate = LocalDateTime.now();

    private String paymentMethod;

    @Min(value = 1, message = "Số lượng khách phải lớn hơn 0")
    private int numberOfGuests;

    private boolean deleted;

    public boolean isActive() {
        return this.status == BookingStatus.CONFIRMED;
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
}
