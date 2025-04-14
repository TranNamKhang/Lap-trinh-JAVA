package com.homestay.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull; // Keep for other fields
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; // Import BigDecimal

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm FetchType LAZY để tối ưu
    @JoinColumn(name = "user_id", nullable = false)
    // @NotNull // Remove this line
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm FetchType LAZY
    @JoinColumn(name = "homestay_id", nullable = false)
    @NotNull // Giữ NotNull cho homestay
    private Homestay homestay;

    @NotNull(message = "Ngày nhận phòng không được để trống") // Giữ NotNull
    private LocalDate checkIn;

    @NotNull(message = "Ngày trả phòng không được để trống") // Giữ NotNull
    private LocalDate checkOut;

    @NotNull(message = "Tổng tiền không được null") // Giữ NotNull
    @Min(value = 0, message="Tổng tiền không hợp lệ") // Có thể thêm Min = 0
    private double totalPrice; // Hoặc dùng BigDecimal nếu muốn chính xác tuyệt đối

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING; // Trạng thái mặc định

    private LocalDateTime bookingDate; // Sẽ set trong @PrePersist hoặc Service

    @NotNull(message = "Vui lòng chọn phương thức thanh toán") // Giữ NotNull
    private String paymentMethod;

    @NotNull(message="Số lượng khách không được để trống") // Giữ NotNull
    @Min(value = 1, message = "Số lượng khách phải ít nhất là 1")
    private int numberOfGuests;

    private boolean deleted = false; // Mặc định là false

    // Bỏ @NotNull ở đây vì sẽ set trong Controller/Service
    private LocalDate appointmentDate;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Thêm FetchType LAZY
    private List<Payment> payments = new ArrayList<>();

    // --- Lifecycle Callback ---
    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now(); // Tự động đặt thời gian tạo
        if (status == null) {
            status = BookingStatus.PENDING; // Đảm bảo có trạng thái mặc định
        }
    }

    // --- Methods ---
    public boolean isActive() {
        return this.status == BookingStatus.CONFIRMED;
    }

    public void cancelBooking() {
        if (this.status == BookingStatus.CONFIRMED || this.status == BookingStatus.PENDING) {
            this.status = BookingStatus.CANCELLED;
        } else {
            throw new IllegalStateException("Không thể hủy đặt phòng ở trạng thái " + (this.status != null ? this.status.getDescription() : "không xác định") + ".");
        }
    }

    // --- Enum BookingStatus ---
    public enum BookingStatus {
        PENDING("Đang chờ xác nhận"),
        CONFIRMED("Đã xác nhận"),
        CANCELLED("Đã hủy"),
        COMPLETED("Hoàn thành");

        private final String description;

        BookingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // --- Getters and Setters ---
    // (Giữ nguyên tất cả getters và setters)

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
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }
    public void addPayment(Payment payment) { payments.add(payment); payment.setBooking(this); }
    public void removePayment(Payment payment) { payments.remove(payment); payment.setBooking(null); }
}