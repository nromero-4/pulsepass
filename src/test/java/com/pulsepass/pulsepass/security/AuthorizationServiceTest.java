package com.pulsepass.pulsepass.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.enums.EventCategory;
import com.pulsepass.pulsepass.enums.EventStatus;
import com.pulsepass.pulsepass.enums.RoleName;
import com.pulsepass.pulsepass.enums.TicketStatus;
import com.pulsepass.pulsepass.enums.TicketType;
import com.pulsepass.pulsepass.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class AuthorizationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthorizationService authorizationService = new AuthorizationService(userRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userCanPurchaseOnlyForTheirOwnEmail() {
        User user = user(1L, "andrea@example.com", RoleName.USER);
        when(userRepository.findByEmailIgnoreCase("ANDREA@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("carlos@example.com"))
                .thenReturn(Optional.of(user(2L, "carlos@example.com", RoleName.USER)));
        authenticate(user);

        assertThatCode(() -> authorizationService.requireCanPurchaseFor("ANDREA@example.com"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> authorizationService.requireCanPurchaseFor("carlos@example.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void userCanAccessOnlyTheirOwnTicket() {
        User owner = user(1L, "andrea@example.com", RoleName.USER);
        authenticate(owner);

        assertThatCode(() -> authorizationService.requireCanAccessTicket(ticket(owner)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> authorizationService.requireCanAccessTicket(ticket(
                user(2L, "carlos@example.com", RoleName.USER))))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void staffCanUseTicketsButCannotPurchaseForAnotherAccount() {
        authenticate(user(1L, "staff@example.com", RoleName.STAFF));
        when(userRepository.findByEmailIgnoreCase("carlos@example.com"))
                .thenReturn(Optional.of(user(2L, "carlos@example.com", RoleName.USER)));

        assertThatCode(authorizationService::requireStaffOrAdmin).doesNotThrowAnyException();
        assertThatThrownBy(() -> authorizationService.requireCanPurchaseFor("carlos@example.com"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> authorizationService.requireCanAccessUserEmail("carlos@example.com"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> authorizationService.requireCanCancelTicket(
                ticket(user(2L, "carlos@example.com", RoleName.USER))))
                .isInstanceOf(AccessDeniedException.class);
        assertThatCode(() -> authorizationService.requireCanAccessTicket(
                ticket(user(2L, "carlos@example.com", RoleName.USER))))
                .doesNotThrowAnyException();
    }

    @Test
    void userCanAccessOnlyTheirOwnPersonalData() {
        User user = user(1L, "andrea@example.com", RoleName.USER);
        when(userRepository.findByEmailIgnoreCase("ANDREA@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("user-1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("carlos@example.com"))
                .thenReturn(Optional.of(user(2L, "carlos@example.com", RoleName.USER)));
        when(userRepository.findByUsername("carlos"))
                .thenReturn(Optional.of(user(2L, "carlos@example.com", RoleName.USER)));
        authenticate(user);
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authorizationService.canAccessEmail(authentication, "ANDREA@example.com")).isTrue();
        assertThat(authorizationService.canAccessUsername(authentication, "user-1")).isTrue();
        assertThat(authorizationService.canAccessEmail(authentication, "carlos@example.com")).isFalse();
        assertThat(authorizationService.canAccessUsername(authentication, "carlos")).isFalse();
    }

    @Test
    void matchingEmailDoesNotReplacePersistentIdentityComparison() {
        User principalUser = user(1L, "andrea@example.com", RoleName.USER);
        when(userRepository.findByEmailIgnoreCase("andrea@example.com"))
                .thenReturn(Optional.of(user(2L, "andrea@example.com", RoleName.USER)));
        authenticate(principalUser);
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authorizationService.canAccessEmail(authentication, "andrea@example.com")).isFalse();
        assertThatThrownBy(() -> authorizationService.requireCanPurchaseFor("andrea@example.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminCanAccessAnotherUsersTicketsButStaffCannotListThem() {
        User owner = user(2L, "carlos@example.com", RoleName.USER);
        when(userRepository.findByEmailIgnoreCase("carlos@example.com")).thenReturn(Optional.of(owner));

        authenticate(user(1L, "admin@example.com", RoleName.ADMIN));
        assertThatCode(() -> authorizationService.requireCanAccessUserEmail("carlos@example.com"))
                .doesNotThrowAnyException();
        assertThatCode(() -> authorizationService.requireCanCancelTicket(ticket(owner)))
                .doesNotThrowAnyException();

        authenticate(user(3L, "staff@example.com", RoleName.STAFF));
        assertThatThrownBy(() -> authorizationService.requireCanAccessUserEmail("carlos@example.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void userCannotCancelAnotherUsersTicketEvenWhenTicketCodeIsKnown() {
        authenticate(user(1L, "andrea@example.com", RoleName.USER));

        assertThatThrownBy(() -> authorizationService.requireCanCancelTicket(
                ticket(user(2L, "carlos@example.com", RoleName.USER))))
                .isInstanceOf(AccessDeniedException.class);
    }

    private void authenticate(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private User user(Long id, String email, RoleName role) {
        User user = new User("user-" + id, email);
        ReflectionTestUtils.setField(user, "id", id);
        user.getRoles().clear();
        user.addRole(role);
        return user;
    }

    private Ticket ticket(User owner) {
        Event event = new Event("EV-1", "Event", "Description", EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.now().plusDays(2), 0,
                new Venue("VEN-1", "Venue", "City", "Address", 10));
        return new Ticket("TCK-1", TicketType.GENERAL, BigDecimal.TEN, TicketStatus.PAID,
                LocalDateTime.now(), owner, event);
    }
}
