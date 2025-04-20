package com.homestay.repositories;

import com.homestay.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByBookingId(Long bookingId);
    List<Ticket> findByPrinted(boolean printed);
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    List<Ticket> findByBookingIdAndPrintedFalse(Long bookingId);
    void deleteByBookingId(Long bookingId);
    void deleteByTicketNumber(String ticketNumber);
}