package com.homestay.repositories;

import com.homestay.models.Booking;
import com.homestay.models.Booking.BookingStatus;
import com.homestay.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByHomestayId(Long homestayId);

    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") BookingStatus status);
    void deleteByHomestayId(Long homestayId);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.user.id = ?1")
    void deleteByUserId(Long userId);
}