package com.pulsepass.pulsepass.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import com.pulsepass.pulsepass.service.ArtistService;
import com.pulsepass.pulsepass.service.AuthService;
import com.pulsepass.pulsepass.service.EventService;
import com.pulsepass.pulsepass.service.TicketService;
import com.pulsepass.pulsepass.service.UserService;
import com.pulsepass.pulsepass.service.VenueService;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.service.impl.UserDetailsServiceImpl;
import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(controllers = {
        ArtistController.class,
        AuthController.class,
        EventController.class,
        TicketController.class,
        UserController.class,
        VenueController.class
})
@Import(MvcSecurityTestConfiguration.class)
class SecurityEndpointMatrixTest {

    private static final String VALID_REGISTRATION = """
            {"username":"andrea","email":"andrea@example.com",
             "password":"Strong-Test-Passphrase-2026!","firstName":"Andrea",
             "lastName":"Lopez","birthDate":"2000-01-01"}
            """;
    private static final String VALID_EVENT = """
            {"eventCode":"EV-1","name":"Event","description":"Description",
             "category":"MUSIC","eventDate":"2030-10-01T19:00:00",
             "minimumAge":18,"venueCode":"VEN-1"}
            """;
    private static final String VALID_PURCHASE = """
            {"userEmail":"andrea@example.com","eventCode":"EV-1","type":"GENERAL"}
            """;

    private static final List<Endpoint> LEGACY_ENDPOINTS = List.of(
            new Endpoint(HttpMethod.GET, "/api/venues/VEN-1", null, 200),
            new Endpoint(HttpMethod.GET, "/api/venues/active", null, 200),
            new Endpoint(HttpMethod.POST, "/api/events", VALID_EVENT, 401),
            new Endpoint(HttpMethod.GET, "/api/events/EV-1", null, 200),
            new Endpoint(HttpMethod.GET, "/api/events/published", null, 200),
            new Endpoint(HttpMethod.PATCH, "/api/events/EV-1/publish", null, 401),
            new Endpoint(HttpMethod.POST, "/api/events/EV-1/artists/1", null, 401),
            new Endpoint(HttpMethod.GET, "/api/events/by-artist?stageName=Solar%20Beat", null, 200),
            new Endpoint(HttpMethod.GET, "/api/artists/1", null, 200),
            new Endpoint(HttpMethod.GET, "/api/artists/by-stage-name?stageName=Solar%20Beat", null, 200),
            new Endpoint(HttpMethod.GET, "/api/artists/active", null, 200),
            new Endpoint(HttpMethod.POST, "/api/users", VALID_REGISTRATION, 201),
            new Endpoint(HttpMethod.GET, "/api/users/by-email?email=andrea%40example.com", null, 401),
            new Endpoint(HttpMethod.GET, "/api/users/by-username?username=andrea", null, 401),
            new Endpoint(HttpMethod.POST, "/api/tickets", VALID_PURCHASE, 401),
            new Endpoint(HttpMethod.GET, "/api/tickets/TCK-1", null, 401),
            new Endpoint(HttpMethod.GET, "/api/tickets/by-user?email=andrea%40example.com", null, 401),
            new Endpoint(HttpMethod.GET, "/api/events/EV-1/tickets/paid", null, 401),
            new Endpoint(HttpMethod.PATCH, "/api/tickets/TCK-1/cancel", null, 401),
            new Endpoint(HttpMethod.PATCH, "/api/tickets/TCK-1/use", null, 401));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistService artistService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private VenueService venueService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void anonymousAccessMatchesThePolicyForAllTwentyLegacyEndpoints() throws Exception {
        assertThat(LEGACY_ENDPOINTS).hasSize(20);
        for (Endpoint endpoint : LEGACY_ENDPOINTS) {
            int actualStatus = mockMvc.perform(request(endpoint.method(), endpoint.path())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(endpoint.body() == null ? "" : endpoint.body()))
                    .andReturn()
                    .getResponse()
                    .getStatus();
            assertThat(actualStatus)
                    .as("%s %s anonymous response", endpoint.method(), endpoint.path())
                    .isEqualTo(endpoint.anonymousStatus());
        }
    }

    @Test
    void regularUsersAndStaffCannotInvokeAdminOnlyEventOperations() throws Exception {
        List<Endpoint> adminOnlyEventEndpoints = List.of(
                new Endpoint(HttpMethod.POST, "/api/events", VALID_EVENT, 403),
                new Endpoint(HttpMethod.PATCH, "/api/events/EV-1/publish", null, 403),
                new Endpoint(HttpMethod.POST, "/api/events/EV-1/artists/1", null, 403));

        for (String role : List.of("USER", "STAFF")) {
            for (Endpoint endpoint : adminOnlyEventEndpoints) {
                int actualStatus = mockMvc.perform(asRole(endpoint, role))
                        .andReturn()
                        .getResponse()
                        .getStatus();
                assertThat(actualStatus)
                        .as("%s %s as %s", endpoint.method(), endpoint.path(), role)
                        .isEqualTo(endpoint.anonymousStatus());
            }
        }

        verify(eventService, never()).create(org.mockito.ArgumentMatchers.any(CreateEventRequest.class));
        verify(eventService, never()).publish("EV-1");
        verify(eventService, never()).addArtist("EV-1", 1L);
    }

    @Test
    void ordinaryUsersCannotUseStaffTicketOperations() throws Exception {
        for (Endpoint endpoint : List.of(
                new Endpoint(HttpMethod.GET, "/api/events/EV-1/tickets/paid", null, 403),
                new Endpoint(HttpMethod.PATCH, "/api/tickets/TCK-1/use", null, 403))) {
            int actualStatus = mockMvc.perform(asRole(endpoint, "USER"))
                    .andReturn()
                    .getResponse()
                    .getStatus();
            assertThat(actualStatus)
                    .as("%s %s as USER", endpoint.method(), endpoint.path())
                    .isEqualTo(403);
        }

        verify(ticketService, never()).findPaidTicketsByEvent("EV-1");
        verify(ticketService, never()).markAsUsed("TCK-1");
    }

    @Test
    void staffCanPerformStaffTicketOperations() throws Exception {
        for (Endpoint endpoint : List.of(
                new Endpoint(HttpMethod.GET, "/api/events/EV-1/tickets/paid", null, 200),
                new Endpoint(HttpMethod.PATCH, "/api/tickets/TCK-1/use", null, 200))) {
            int actualStatus = mockMvc.perform(asRole(endpoint, "STAFF"))
                    .andReturn()
                    .getResponse()
                    .getStatus();
            assertThat(actualStatus)
                    .as("%s %s as STAFF", endpoint.method(), endpoint.path())
                    .isEqualTo(200);
        }

        verify(ticketService).findPaidTicketsByEvent("EV-1");
        verify(ticketService).markAsUsed("TCK-1");
    }

    private MockHttpServletRequestBuilder asRole(Endpoint endpoint, String role) {
        var builder = request(endpoint.method(), endpoint.path())
                .with(SecurityMockMvcRequestPostProcessors.user(role.toLowerCase()).roles(role))
                .contentType(MediaType.APPLICATION_JSON);
        if (endpoint.body() != null) {
            builder.content(endpoint.body());
        }
        return builder;
    }

    private record Endpoint(HttpMethod method, String path, String body, int anonymousStatus) {
    }
}
