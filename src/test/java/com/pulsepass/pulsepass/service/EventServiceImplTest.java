package com.pulsepass.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.pulsepass.enums.EventCategory;
import com.pulsepass.pulsepass.enums.EventStatus;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.EventMapper;
import com.pulsepass.pulsepass.repository.ArtistRepository;
import com.pulsepass.pulsepass.repository.EventRepository;
import com.pulsepass.pulsepass.repository.VenueRepository;
import com.pulsepass.pulsepass.service.impl.EventServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void findByCodeReturnsMappedEvent() {
        Event event = event(EventStatus.DRAFT, activeVenue());
        EventResponse response = response(EventStatus.DRAFT);
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse result = eventService.findByCode("EV-1");

        assertThat(result).isSameAs(response);
    }

    @Test
    void findByCodeThrowsWhenEventDoesNotExist() {
        when(eventRepository.findByEventCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findByCode("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void findPublishedEventsReturnsMappedSummaries() {
        Event event = event(EventStatus.PUBLISHED, activeVenue());
        EventSummaryResponse summary = summaryResponse();
        when(eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED)).thenReturn(List.of(event));
        when(eventMapper.toSummary(event)).thenReturn(summary);

        List<EventSummaryResponse> result = eventService.findPublishedEvents();

        assertThat(result).containsExactly(summary);
        verify(eventRepository).findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);
    }

    @Test
    void createSavesNewEventAsDraft() {
        Venue venue = activeVenue();
        CreateEventRequest request = createRequest(futureDate());
        when(eventRepository.existsByEventCode("EV-1")).thenReturn(false);
        when(venueRepository.findByCode("VEN-1")).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventMapper.toResponse(any(Event.class))).thenReturn(response(EventStatus.DRAFT));

        EventResponse result = eventService.create(request);

        assertThat(result.status()).isEqualTo(EventStatus.DRAFT);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createRejectsDuplicateEventCode() {
        when(eventRepository.existsByEventCode("EV-1")).thenReturn(true);

        assertThatThrownBy(() -> eventService.create(createRequest(futureDate())))
                .isInstanceOf(DuplicateResourceException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createRejectsMissingVenueWithoutSaving() {
        when(eventRepository.existsByEventCode("EV-1")).thenReturn(false);
        when(venueRepository.findByCode("VEN-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.create(createRequest(futureDate())))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createRejectsInactiveVenue() {
        Venue venue = activeVenue();
        venue.setActive(false);
        when(eventRepository.existsByEventCode("EV-1")).thenReturn(false);
        when(venueRepository.findByCode("VEN-1")).thenReturn(Optional.of(venue));

        assertThatThrownBy(() -> eventService.create(createRequest(futureDate())))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createRejectsPastDate() {
        when(eventRepository.existsByEventCode("EV-1")).thenReturn(false);
        when(venueRepository.findByCode("VEN-1")).thenReturn(Optional.of(activeVenue()));

        assertThatThrownBy(() -> eventService.create(createRequest(LocalDateTime.now().minusDays(1))))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createRejectsNegativeMinimumAge() {
        when(eventRepository.existsByEventCode("EV-1")).thenReturn(false);
        when(venueRepository.findByCode("VEN-1")).thenReturn(Optional.of(activeVenue()));
        CreateEventRequest request = new CreateEventRequest("EV-1", "Event", "Description",
                EventCategory.MUSIC, futureDate(), -1, "VEN-1");

        assertThatThrownBy(() -> eventService.create(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void publishChangesDraftToPublished() {
        Event event = event(EventStatus.DRAFT, activeVenue());
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response(EventStatus.PUBLISHED));

        EventResponse result = eventService.publish("EV-1");

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(result.status()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(event);
    }

    @Test
    void publishRejectsCancelledEventWithoutSaving() {
        Event event = event(EventStatus.CANCELLED, activeVenue());
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.publish("EV-1"))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void publishRejectsPastEventWithoutSaving() {
        Event event = event(EventStatus.DRAFT, activeVenue());
        event.setEventDate(LocalDateTime.now().minusDays(1));
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.publish("EV-1"))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void publishRejectsInactiveVenueWithoutSaving() {
        Venue venue = activeVenue();
        venue.setActive(false);
        Event event = event(EventStatus.DRAFT, venue);
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.publish("EV-1"))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void addArtistAssociatesArtistAndSavesEvent() {
        Event event = event(EventStatus.DRAFT, activeVenue());
        Artist artist = new Artist("Solar Beat", "Colombia", "Pop");
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response(EventStatus.DRAFT));

        eventService.addArtist("EV-1", 1L);

        assertThat(event.getArtists()).contains(artist);
        verify(eventRepository).save(event);
    }

    @Test
    void addArtistRejectsDuplicateAssociationWithoutSaving() {
        Event event = event(EventStatus.DRAFT, activeVenue());
        Artist artist = new Artist("Solar Beat", "Colombia", "Pop");
        event.getArtists().add(artist);
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        assertThatThrownBy(() -> eventService.addArtist("EV-1", 1L))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void addArtistRejectsCancelledEventWithoutSaving() {
        Event event = event(EventStatus.CANCELLED, activeVenue());
        Artist artist = new Artist("Solar Beat", "Colombia", "Pop");
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        assertThatThrownBy(() -> eventService.addArtist("EV-1", 1L))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void addArtistRejectsFinishedEventWithoutSaving() {
        Event event = event(EventStatus.FINISHED, activeVenue());
        Artist artist = new Artist("Solar Beat", "Colombia", "Pop");
        when(eventRepository.findByEventCode("EV-1")).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        assertThatThrownBy(() -> eventService.addArtist("EV-1", 1L))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void findByArtistReturnsMappedSummaries() {
        Event event = event(EventStatus.PUBLISHED, activeVenue());
        EventSummaryResponse summary = summaryResponse();
        when(eventRepository.findByArtistStageName("Solar Beat")).thenReturn(List.of(event));
        when(eventMapper.toSummary(event)).thenReturn(summary);

        List<EventSummaryResponse> result = eventService.findByArtist("Solar Beat");

        assertThat(result).containsExactly(summary);
        verify(eventRepository).findByArtistStageName("Solar Beat");
    }

    private CreateEventRequest createRequest(LocalDateTime eventDate) {
        return new CreateEventRequest("EV-1", "Event", "Description", EventCategory.MUSIC,
                eventDate, 18, "VEN-1");
    }

    private Event event(EventStatus status, Venue venue) {
        return new Event("EV-1", "Event", "Description", EventCategory.MUSIC,
                status, futureDate(), 18, venue);
    }

    private Venue activeVenue() {
        return new Venue("VEN-1", "Venue", "Santa Marta", "Address", 100);
    }

    private LocalDateTime futureDate() {
        return LocalDateTime.now().plusDays(30);
    }

    private EventResponse response(EventStatus status) {
        return new EventResponse(1L, "EV-1", "Event", "Description", EventCategory.MUSIC,
                status, futureDate(), 18, "VEN-1", "Venue", List.of());
    }

    private EventSummaryResponse summaryResponse() {
        return new EventSummaryResponse(1L, "EV-1", "Event", "Description", EventCategory.MUSIC,
                EventStatus.PUBLISHED, futureDate(), 18, "VEN-1", "Venue", List.of());
    }
}