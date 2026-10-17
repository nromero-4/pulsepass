package com.pulsepass.pulsepass.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.pulsepass.enums.EventCategory;
import com.pulsepass.pulsepass.enums.EventStatus;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.service.EventService;
import com.pulsepass.pulsepass.service.impl.UserDetailsServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(EventController.class)
@Import(MvcSecurityTestConfiguration.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void regularUserCannotPublishEvents() throws Exception {
        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026")
                        .with(SecurityMockMvcRequestPostProcessors.user("andrea").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        verify(eventService, never()).publish("CMF-2026");
    }

    @Test
    void createReturnsCreatedEvent() throws Exception {
        CreateEventRequest request = validRequest();
        when(eventService.create(request)).thenReturn(event(EventStatus.DRAFT));

        mockMvc.perform(post("/api/events")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(eventService).create(request);
    }

    @Test
    void createRejectsInvalidRequestWithoutCallingService() throws Exception {
        String invalidRequest = """
                {"eventCode":"", "name":"", "description":"Fest", "category":null,
                 "eventDate":null, "minimumAge":-1, "venueCode":""}
                """;

        mockMvc.perform(post("/api/events")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.eventCode").exists())
                .andExpect(jsonPath("$.details.minimumAge").exists());

        verify(eventService, never()).create(any(CreateEventRequest.class));
    }

    @Test
    void findByCodeReturnsEvent() throws Exception {
        when(eventService.findByCode("CMF-2026")).thenReturn(event(EventStatus.DRAFT));

        mockMvc.perform(get("/api/events/{eventCode}", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        verify(eventService).findByCode("CMF-2026");
    }

    @Test
    void findByCodeReturnsNotFoundResponse() throws Exception {
        when(eventService.findByCode("UNKNOWN"))
                .thenThrow(new ResourceNotFoundException("Event not found: UNKNOWN"));

        mockMvc.perform(get("/api/events/{eventCode}", "UNKNOWN")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Event not found: UNKNOWN"));

        verify(eventService).findByCode("UNKNOWN");
    }

    @Test
    void findPublishedEventsReturnsSummaries() throws Exception {
        when(eventService.findPublishedEvents()).thenReturn(List.of(summary(EventStatus.PUBLISHED)));

        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"));

        verify(eventService).findPublishedEvents();
    }

    @Test
    void publishReturnsUpdatedEvent() throws Exception {
        when(eventService.publish("CMF-2026")).thenReturn(event(EventStatus.PUBLISHED));

        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(eventService).publish("CMF-2026");
    }

    @Test
    void publishReturnsConflictWhenServiceRejectsTransition() throws Exception {
        when(eventService.publish("CMF-2026"))
                .thenThrow(new BusinessRuleException("Only draft events can be published"));

        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Only draft events can be published"));

        verify(eventService).publish("CMF-2026");
    }

    @Test
    void addArtistReturnsUpdatedEvent() throws Exception {
        when(eventService.addArtist("CMF-2026", 1L)).thenReturn(event(EventStatus.DRAFT));

        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 1)
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        verify(eventService).addArtist("CMF-2026", 1L);
    }

        @Test
        void addArtistReturnsConflictWhenServiceRejectsAssociation() throws Exception {
                when(eventService.addArtist("CMF-2026", 1L))
                                .thenThrow(new BusinessRuleException("Artist is already associated with event"));

                mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 1)
                                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.message").value("Artist is already associated with event"));

                verify(eventService).addArtist("CMF-2026", 1L);
        }

    @Test
    void findByArtistReturnsMatchingSummaries() throws Exception {
        when(eventService.findByArtist("Solar Beat")).thenReturn(List.of(summary(EventStatus.PUBLISHED)));

        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(eventService).findByArtist("Solar Beat");
    }

    private CreateEventRequest validRequest() {
        return new CreateEventRequest("CMF-2026", "Caribbean Music Fest 2026", "Music festival",
                EventCategory.MUSIC, LocalDateTime.of(2026, 11, 1, 19, 0), 18, "VEN-SMR-01");
    }

    private EventResponse event(EventStatus status) {
        return new EventResponse(1L, "CMF-2026", "Caribbean Music Fest 2026", "Music festival",
                EventCategory.MUSIC, status, LocalDateTime.of(2026, 11, 1, 19, 0), 18,
                "VEN-SMR-01", "Marina Convention Center", List.of("Solar Beat"));
    }

    private EventSummaryResponse summary(EventStatus status) {
        return new EventSummaryResponse(1L, "CMF-2026", "Caribbean Music Fest 2026", "Music festival",
                EventCategory.MUSIC, status, LocalDateTime.of(2026, 11, 1, 19, 0), 18,
                "VEN-SMR-01", "Marina Convention Center", List.of("Solar Beat"));
    }
}