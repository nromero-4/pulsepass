package com.pulsepass.pulsepass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserCredential;
import com.pulsepass.pulsepass.domain.UserProfile;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.dto.request.LoginRequest;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.enums.EventCategory;
import com.pulsepass.pulsepass.enums.EventStatus;
import com.pulsepass.pulsepass.enums.TicketStatus;
import com.pulsepass.pulsepass.enums.TicketType;
import com.pulsepass.pulsepass.repository.ArtistRepository;
import com.pulsepass.pulsepass.repository.EventRepository;
import com.pulsepass.pulsepass.repository.TicketRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.pulsepass.repository.VenueRepository;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.security.TestJwtSecret;
import com.pulsepass.pulsepass.service.AuthService;
import com.pulsepass.pulsepass.service.UserService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @DynamicPropertySource
    static void registerTestJwtSecret(DynamicPropertyRegistry registry) {
        TestJwtSecret.register(registry);
    }

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void shouldPersistVenueAndEventWithArtistRelations() {
        Venue venue = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Calle 1", 5000);
        venueRepository.save(venue);

        Artist solarBeat = artistRepository.findByStageNameIgnoreCase("Solar Beat")
            .orElseGet(() -> artistRepository.save(new Artist("Solar Beat", "Colombia", "Electronic")));
        Artist neonWaves = artistRepository.findByStageNameIgnoreCase("Neon Waves")
            .orElseGet(() -> artistRepository.save(new Artist("Neon Waves", "Argentina", "Synth Pop")));

        Event event = new Event(
            "CMF-2026",
            "Caribbean Music Fest 2026",
            "Festival de música",
            EventCategory.MUSIC,
            EventStatus.PUBLISHED,
            LocalDateTime.of(2026, 6, 15, 19, 0),
            18,
            venue
        );
        event.setArtists(Set.of(solarBeat, neonWaves));
        eventRepository.save(event);

        Event loaded = eventRepository.findByEventCode("CMF-2026").orElseThrow();
        assertNotNull(loaded.getVenue());
        assertEquals("VEN-SMR-01", loaded.getVenue().getCode());
        assertEquals(2, loaded.getArtists().size());
        assertEquals(1, eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED).size());
    }

    @Test
    void shouldPersistUserWithProfileAndTicket() {
        Venue venue = venueRepository.save(new Venue("VEN-USER-01", "Arena Center", "Bogota", "Calle 10", 1200));
        Event event = eventRepository.save(new Event(
            "EVT-USER-01",
            "User Event",
            "Evento de ejemplo",
            EventCategory.ENTERTAINMENT,
            EventStatus.PUBLISHED,
            LocalDateTime.of(2026, 9, 10, 19, 0),
            16,
            venue
        ));

        User user = userRepository.save(new User("andrea", "andrea@email.com"));
        user.setProfile(new UserProfile("Andrea", "Lopez", "3000000000", "Bogota", LocalDate.of(1995, 2, 10), user));

        Ticket ticket = ticketRepository.save(new Ticket(
            "TCK-0001",
            TicketType.VIP,
            new BigDecimal("250000.00"),
            TicketStatus.PAID,
            LocalDateTime.now(),
            user,
            event
        ));

        assertNotNull(userRepository.findByEmailIgnoreCase("ANDREA@EMAIL.COM"));
        assertEquals("Andrea", user.getProfile().getFirstName());
        assertEquals("TCK-0001", ticket.getTicketCode());
    }

    @Test
    void shouldPersistCredentialSeparatelyFromUser() {
        User user = userRepository.save(new User("credential-user", "credential@email.com"));
        UserCredential credential = userCredentialRepository.save(
                new UserCredential(user, "$2a$12$test-only-hash"));

        assertNotNull(userCredentialRepository.findById(user.getId()).orElse(null));
        assertEquals(user.getId(), credential.getUserId());
        assertEquals("$2a$12$test-only-hash", credential.getPasswordHash());
    }

    @Test
    void shouldRegisterAndAuthenticateUsingPersistedBcryptCredential() {
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        String password = "A-Strong-Passphrase-2026!";
        var registered = userService.register(new RegisterUserRequest(
                "secure-" + suffix, suffix + "@example.com", password, "Test", "User",
                null, null, LocalDate.of(1990, 1, 1)));
        UserCredential credential = userCredentialRepository.findById(registered.id()).orElseThrow();

        assertTrue(passwordEncoder.matches(password, credential.getPasswordHash()));
        assertEquals(Set.of(com.pulsepass.pulsepass.enums.RoleName.USER),
                userRepository.findById(registered.id()).orElseThrow().getRoles());

        var response = authService.login(new LoginRequest(suffix + "@example.com", password));
        assertEquals("Bearer", response.tokenType());
        assertTrue(jwtService.isTokenValid(response.accessToken()));
        assertEquals(registered.id(), jwtService.extractUserId(response.accessToken()));
    }

    @Test
    void shouldFindEventsByArtistAndTicketSales() {
        Venue venue = venueRepository.save(new Venue("VEN-SALES-01", "Sales Arena", "Medellin", "Cra 1", 1000));
        Artist artist = artistRepository.findByStageNameIgnoreCase("Solar Beat")
            .orElseGet(() -> artistRepository.save(new Artist("Solar Beat", "Colombia", "Electronic")));

        Event event = eventRepository.save(new Event(
            "EVT-SALES-01",
            "Sales Event",
            "Descripcion",
            EventCategory.MUSIC,
            EventStatus.PUBLISHED,
            LocalDateTime.of(2026, 7, 20, 19, 0),
            18,
            venue
        ));
        event.getArtists().add(artist);
        eventRepository.save(event);

        User user = userRepository.save(new User("carlos", "carlos@email.com"));
        ticketRepository.save(new Ticket("TCK-PAID-01", TicketType.GENERAL, new BigDecimal("120000.00"), TicketStatus.PAID, LocalDateTime.now(), user, event));
        ticketRepository.save(new Ticket("TCK-RES-01", TicketType.GENERAL, new BigDecimal("120000.00"), TicketStatus.RESERVED, LocalDateTime.now(), user, event));

        List<Event> byArtist = eventRepository.findByArtistStageName("Solar Beat");
        List<Ticket> paid = ticketRepository.findByEventCodeAndStatus("EVT-SALES-01", TicketStatus.PAID);

        assertTrue(byArtist.stream().anyMatch(e -> e.getEventCode().equals("EVT-SALES-01")));
        assertEquals(1, paid.size());
        assertEquals(1, ticketRepository.countByEventCodeAndStatus("EVT-SALES-01", TicketStatus.PAID));
    }
}
