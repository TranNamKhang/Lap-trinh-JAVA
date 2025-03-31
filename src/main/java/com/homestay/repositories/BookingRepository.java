package com.homestay.repositories;

import com.homestay.models.Booking;
import com.homestay.models.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByHomestayId(Long homestayId);
    List<Booking> findByStatus(BookingStatus status);
}
