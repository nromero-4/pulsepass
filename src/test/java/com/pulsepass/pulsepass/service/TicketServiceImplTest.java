package com.pulsepass.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserProfile;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.enums.EventCategory;
import com.pulsepass.pulsepass.enums.EventStatus;
import com.pulsepass.pulsepass.enums.TicketStatus;
import com.pulsepass.pulsepass.enums.TicketType;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.TicketMapper;
import com.pulsepass.pulsepass.repository.EventRepository;
import com.pulsepass.pulsepass.repository.TicketRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.service.impl.TicketServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private TicketPricingStrategy pricingStrategy;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void purchaseCreatesPaidTicketAtServerCalculatedPrice() {
        User user = activeAdultUser();
        Event event = event(EventStatus.PUBLISHED, 3);
        PurchaseTicketRequest request = request(TicketType.VIP);
        TicketResponse response = response(TicketStatus.PAID);
        when(userRepository.findByEmailIgnoreCase(request.userEmail())).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(request.eventCode())).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventCodeAndStatus("EV-1", TicketStatus.PAID)).thenReturn(0L);
        when(pricingStrategy.priceFor(TicketType.VIP)).thenReturn(new BigDecimal("250000.00"));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response);

        TicketResponse result = ticketService.purchase(request);

        assertThat(result.status()).isEqualTo(TicketStatus.PAID);
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());
        Ticket savedTicket = ticketCaptor.getValue();
        assertThat(savedTicket.getTicketCode()).startsWith("TCK-");
        assertThat(savedTicket.getType()).isEqualTo(TicketType.VIP);
        assertThat(savedTicket.getStatus()).isEqualTo(TicketStatus.PAID);
        assertThat(savedTicket.getPrice()).isEqualByComparingTo("250000.00");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void purchaseRejectsMissingUserWithoutSaving() {
        when(userRepository.findByEmailIgnoreCase("person@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.purchase(request(TicketType.GENERAL)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchaseRejectsInactiveUserWithoutSaving() {
        User user = activeAdultUser();
        user.setActive(false);
        when(userRepository.findByEmailIgnoreCase("person@email.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> ticketService.purchase(request(TicketType.GENERAL)))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchaseRejectsMissingEventWithoutSaving() {
        when(userRepository.findByEmailIgnoreCase("person@email.com")).thenReturn(Optional.of(activeAdultUser()));
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.purchase(request(TicketType.GENERAL)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchaseRejectsUnpublishedEventWithoutSaving() {
        stubPurchase(activeAdultUser(), event(EventStatus.DRAFT, 3));

        assertThatThrownBy(() -> ticketService.purchase(request(TicketType.GENERAL)))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchaseRejectsUnderageUserWithoutSaving() {
        User underageUser = userWithBirthDate(LocalDate.now().minusYears(17));
        stubPurchase(underageUser, event(EventStatus.PUBLISHED, 3));

        assertThatThrownBy(() -> ticketService.purchase(request(TicketType.GENERAL)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("minimum age");
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchaseRejectsFullCapacityWithoutSaving() {
        stubPurchase(activeAdultUser(), event(EventStatus.PUBLISHED, 2));
        when(ticketRepository.countByEventCodeAndStatus("EV-1", TicketStatus.PAID)).thenReturn(2L);

        assertThatThrownBy(() -> ticketService.purchase(request(TicketType.GENERAL)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("capacity");
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void lastAvailableTicketMarksEventSoldOut() {
        User user = activeAdultUser();
        Event event = event(EventStatus.PUBLISHED, 1);
        stubPurchase(user, event);
        when(ticketRepository.countByEventCodeAndStatus("EV-1", TicketStatus.PAID)).thenReturn(0L);
        when(pricingStrategy.priceFor(TicketType.GENERAL)).thenReturn(new BigDecimal("100000.00"));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response(TicketStatus.PAID));

        ticketService.purchase(request(TicketType.GENERAL));

        assertThat(event.getStatus()).isEqualTo(EventStatus.SOLD_OUT);
        verify(ticketRepository).save(any(Ticket.class));
        verify(eventRepository).save(event);
    }

    @Test
    void findByCodeReturnsMappedTicket() {
        Ticket ticket = ticket(TicketStatus.PAID, event(EventStatus.PUBLISHED, 3));
        TicketResponse response = response(TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(response);

        assertThat(ticketService.findByCode("TCK-1")).isSameAs(response);
    }

    @Test
    void findByCodeThrowsWhenTicketDoesNotExist() {
        when(ticketRepository.findByTicketCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.findByCode("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByUserEmailMapsTickets() {
        Ticket ticket = ticket(TicketStatus.PAID, event(EventStatus.PUBLISHED, 3));
        when(ticketRepository.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc("person@email.com"))
                .thenReturn(List.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(response(TicketStatus.PAID));

        assertThat(ticketService.findByUserEmail("person@email.com")).hasSize(1);
    }

    @Test
    void findPaidTicketsByEventFiltersByPaidStatus() {
        when(ticketRepository.findByEventCodeAndStatus("EV-1", TicketStatus.PAID)).thenReturn(List.of());

        assertThat(ticketService.findPaidTicketsByEvent("EV-1")).isEmpty();
        verify(ticketRepository).findByEventCodeAndStatus("EV-1", TicketStatus.PAID);
    }

    @Test
    void cancelChangesPaidTicketToCancelled() {
        Ticket ticket = ticket(TicketStatus.PAID, event(EventStatus.PUBLISHED, 3));
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(response(TicketStatus.CANCELLED));

        TicketResponse result = ticketService.cancel("TCK-1");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(result.status()).isEqualTo(TicketStatus.CANCELLED);
    }

    @Test
    void cancelRejectsUsedTicketWithoutSaving() {
        Ticket ticket = ticket(TicketStatus.USED, event(EventStatus.PUBLISHED, 3));
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancel("TCK-1"))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void cancelRejectsTicketAfterEventDateWithoutSaving() {
        Event pastEvent = event(EventStatus.PUBLISHED, 3);
        pastEvent.setEventDate(LocalDate.now().minusDays(1));
        Ticket ticket = ticket(TicketStatus.PAID, pastEvent);
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancel("TCK-1"))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void markAsUsedChangesPaidTicketToUsed() {
        Ticket ticket = ticket(TicketStatus.PAID, event(EventStatus.PUBLISHED, 3));
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(response(TicketStatus.USED));

        TicketResponse result = ticketService.markAsUsed("TCK-1");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
        assertThat(result.status()).isEqualTo(TicketStatus.USED);
    }

    @Test
    void markAsUsedRejectsCancelledTicketWithoutSaving() {
        Ticket ticket = ticket(TicketStatus.CANCELLED, event(EventStatus.PUBLISHED, 3));
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.markAsUsed("TCK-1"))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    private void stubPurchase(User user, Event event) {
        when(userRepository.findByEmailIgnoreCase("person@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));
    }

    private PurchaseTicketRequest request(TicketType type) {
        return new PurchaseTicketRequest("person@email.com", "EV-1", type);
    }

    private User activeAdultUser() {
        return userWithBirthDate(LocalDate.now().minusYears(25));
    }

    private User userWithBirthDate(LocalDate birthDate) {
        User user = new User("person", "person@email.com");
        user.setProfile(new UserProfile("Person", "Example", "3000000000", "Santa Marta", birthDate, user));
        return user;
    }

    private Event event(EventStatus status, int capacity) {
        Venue venue = new Venue("VEN-1", "Venue", "Santa Marta", "Address", capacity);
        return new Event("EV-1", "Event", "Description", EventCategory.MUSIC,
                status, LocalDate.now().plusDays(30), 18, venue);
    }

    private Ticket ticket(TicketStatus status, Event event) {
        return new Ticket("TCK-1", TicketType.GENERAL, new BigDecimal("100000.00"), status,
                LocalDateTime.now(), activeAdultUser(), event);
    }

    private TicketResponse response(TicketStatus status) {
        return new TicketResponse(1L, "TCK-1", TicketType.GENERAL, new BigDecimal("100000.00"),
                status, LocalDateTime.now(), "person@email.com", "EV-1", "Event");
    }
}