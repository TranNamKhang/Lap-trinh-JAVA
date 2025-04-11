package com.homestay.repositories;

import com.homestay.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBooking(com.homestay.models.Booking booking);

    // Tìm theo mã giao dịch tham chiếu (transactionRef)
    Optional<Payment> findByTransactionRef(String transactionRef);

    @Modifying
    @Transactional
    @Query("UPDATE Payment p SET p.status = :status WHERE p.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}