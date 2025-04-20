package com.homestay.services;

import com.homestay.models.Booking;
import com.homestay.models.Ticket;
import com.homestay.repositories.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);
    private final TicketRepository ticketRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket generateTicket(Booking booking) {
        List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
        Optional<Ticket> existingTicket = tickets.isEmpty() ? Optional.empty() : Optional.of(tickets.get(0));
        if (existingTicket.isPresent()) {
            logger.info("Ticket already exists for booking ID: {}", booking.getId());
            return existingTicket.get();
        }

        // Create new ticket
        Ticket ticket = new Ticket();
        ticket.setBooking(booking);
        
        // Generate QR code URL (can be implemented with a QR code library)
        String qrCodeUrl = "/tickets/qr/" + booking.getId();
        ticket.setQrCodeUrl(qrCodeUrl);
        
        logger.info("Generated new ticket for booking ID: {}", booking.getId());
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getUnprintedTickets() {
        return ticketRepository.findByPrinted(false);
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public Optional<Ticket> getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber);
    }

    @Transactional
    public boolean markAsPrinted(Long ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setPrinted(true);
            ticketRepository.save(ticket);
            logger.info("Ticket ID: {} marked as printed", ticketId);
            return true;
        }
        logger.warn("Ticket ID: {} not found for printing", ticketId);
        return false;
    }

    public Optional<Ticket> getTicketByBookingId(Long bookingId) {
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        return tickets.isEmpty() ? Optional.empty() : Optional.of(tickets.get(0));
    }

    @Transactional
    public void deleteByBookingId(Long bookingId) {
        ticketRepository.deleteByBookingId(bookingId);
    }
    
    @Transactional
    public void deleteTicketsForBookings(List<Long> bookingIds) {
        for (Long bookingId : bookingIds) {
            deleteByBookingId(bookingId);
        }
    }

    public List<Ticket> getTicketsByBookingId(Long bookingId) {
        return ticketRepository.findByBookingId(bookingId);
    }

    public List<Ticket> getUnprintedTicketsByBookingId(Long bookingId) {
        return ticketRepository.findByBookingIdAndPrintedFalse(bookingId);
    }
}