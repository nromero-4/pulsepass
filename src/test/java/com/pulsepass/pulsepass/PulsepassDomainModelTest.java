package com.pulsepass.pulsepass;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserCredential;
import com.pulsepass.pulsepass.domain.UserProfile;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.enums.EventStatus;
import com.pulsepass.pulsepass.security.TestJwtSecret;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PulsepassDomainModelTest {

    @DynamicPropertySource
    static void registerTestJwtSecret(DynamicPropertyRegistry registry) {
        TestJwtSecret.register(registry);
    }

    @Test
    void domainModelShouldExist() {
        assertNotNull(Venue.class);
        assertNotNull(Event.class);
        assertNotNull(Artist.class);
        assertNotNull(User.class);
        assertNotNull(UserCredential.class);
        assertNotNull(UserProfile.class);
        assertNotNull(Ticket.class);
        assertNotNull(EventStatus.PUBLISHED);
    }
}
