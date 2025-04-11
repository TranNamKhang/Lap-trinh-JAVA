package com.homestay.repositories;

import com.homestay.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Tìm danh sách booking theo trạng thái
    List<Booking> findByStatus(Booking.BookingStatus status);

    // Tìm danh sách booking theo user ID
    List<Booking> findByUserId(Long userId);

    // Tìm danh sách booking theo homestay ID
    List<Booking> findByHomestayId(Long homestayId);

    List<Booking> findByUsername(String username);
}
