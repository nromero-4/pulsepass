package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.enums.EventStatus;
import com.pulsepass.pulsepass.enums.TicketStatus;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.TicketMapper;
import com.pulsepass.pulsepass.repository.EventRepository;
import com.pulsepass.pulsepass.repository.TicketRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.service.TicketPricingStrategy;
import com.pulsepass.pulsepass.service.TicketService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;
    private final TicketPricingStrategy pricingStrategy;

    public TicketServiceImpl(TicketRepository ticketRepository, UserRepository userRepository,
                             EventRepository eventRepository, TicketMapper ticketMapper,
                             TicketPricingStrategy pricingStrategy) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketMapper = ticketMapper;
        this.pricingStrategy = pricingStrategy;
    }

    @Override
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userEmail()));
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessRuleException("Inactive users cannot purchase tickets: " + request.userEmail());
        }

        Event event = eventRepository.findByEventCode(request.eventCode())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + request.eventCode()));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException("Tickets can only be purchased for published events: " + request.eventCode());
        }
        if (event.getEventDate() == null || event.getEventDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Tickets cannot be purchased after the event date: " + request.eventCode());
        }
        validateMinimumAge(user, event);

        Integer capacity = event.getVenue().getCapacity();
        if (capacity == null || capacity <= 0) {
            throw new BusinessRuleException("Event venue has no valid ticket capacity: " + request.eventCode());
        }
        long paidTickets = ticketRepository.countByEventCodeAndStatus(request.eventCode(), TicketStatus.PAID);
        if (paidTickets >= capacity) {
            throw new BusinessRuleException("Event has reached its ticket capacity: " + request.eventCode());
        }

        Ticket ticket = new Ticket("TCK-" + UUID.randomUUID(), request.type(),
                pricingStrategy.priceFor(request.type()), TicketStatus.PAID, LocalDateTime.now(), user, event);
        Ticket savedTicket = ticketRepository.save(ticket);
        if (paidTickets + 1 == capacity) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
        }
        return ticketMapper.toResponse(savedTicket);
    }

    @Override
    public TicketResponse findByCode(String ticketCode) {
        return ticketMapper.toResponse(findTicket(ticketCode));
    }

    @Override
    public List<TicketResponse> findByUserEmail(String email) {
        return ticketRepository.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(email).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode) {
        return ticketRepository.findByEventCodeAndStatus(eventCode, TicketStatus.PAID).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse cancel(String ticketCode) {
        Ticket ticket = findTicket(ticketCode);
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Only paid tickets can be cancelled: " + ticketCode);
        }
        if (ticket.getEvent().getEventDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Tickets cannot be cancelled after the event date: " + ticketCode);
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {
        Ticket ticket = findTicket(ticketCode);
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Only paid tickets can be marked as used: " + ticketCode);
        }

        ticket.setStatus(TicketStatus.USED);
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    private Ticket findTicket(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));
    }

    private void validateMinimumAge(User user, Event event) {
        Integer minimumAge = event.getMinimumAge();
        if (minimumAge == null || minimumAge <= 0) {
            return;
        }

        LocalDate birthDate = user.getProfile() == null ? null : user.getProfile().getBirthDate();
        if (birthDate == null || Period.between(birthDate, event.getEventDate()).getYears() < minimumAge) {
            throw new BusinessRuleException("User does not meet the minimum age for event: " + event.getEventCode());
        }
    }
}
