package com.pulsepass.pulsepass;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserProfile;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.enums.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PulsepassDomainModelTest {

    @Test
    void domainModelShouldExist() {
        assertNotNull(Venue.class);
        assertNotNull(Event.class);
        assertNotNull(Artist.class);
        assertNotNull(User.class);
        assertNotNull(UserProfile.class);
        assertNotNull(Ticket.class);
        assertNotNull(EventStatus.PUBLISHED);
    }
}
