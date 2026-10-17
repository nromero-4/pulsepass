package com.pulsepass.pulsepass.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.pulsepass.dto.response.VenueResponse;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.service.VenueService;
import com.pulsepass.pulsepass.service.impl.UserDetailsServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VenueController.class)
@Import(MvcSecurityTestConfiguration.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void findByCodeReturnsVenue() throws Exception {
        when(venueService.findByCode("VEN-SMR-01")).thenReturn(venue());

        mockMvc.perform(get("/api/venues/{code}", "VEN-SMR-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$.name").value("Marina Convention Center"))
                .andExpect(jsonPath("$.capacity").value(3));

        verify(venueService).findByCode("VEN-SMR-01");
    }

    @Test
    void findByCodeReturnsStandardNotFoundResponse() throws Exception {
        when(venueService.findByCode("UNKNOWN"))
                .thenThrow(new ResourceNotFoundException("Venue not found: UNKNOWN"));

        mockMvc.perform(get("/api/venues/{code}", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Venue not found: UNKNOWN"))
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(venueService).findByCode("UNKNOWN");
    }

    @Test
    void findActiveVenuesReturnsList() throws Exception {
        when(venueService.findActiveVenues()).thenReturn(List.of(venue()));

        mockMvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(venueService).findActiveVenues();
    }

    private VenueResponse venue() {
        return new VenueResponse(1L, "VEN-SMR-01", "Marina Convention Center",
                "Santa Marta", "Centro", 3, true);
    }
}