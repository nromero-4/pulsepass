package com.pulsepass.pulsepass.security;

import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.repository.UserRepository;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component("authorizationService")
public class AuthorizationService {

    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean canAccessEmail(Authentication authentication, String email) {
        UserPrincipal principal = principal(authentication);
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }
        return principal != null && userRepository.findByEmailIgnoreCase(email)
                .map(user -> samePersistentUser(principal.getUser(), user))
                .orElse(false);
    }

    public boolean canAccessUsername(Authentication authentication, String username) {
        UserPrincipal principal = principal(authentication);
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }
        return principal != null && userRepository.findByUsername(username)
                .map(user -> samePersistentUser(principal.getUser(), user))
                .orElse(false);
    }

    public void requireCanPurchaseFor(String email) {
        Authentication authentication = currentAuthentication();
        UserPrincipal principal = principal(authentication);
        User requestedUser = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (principal == null || requestedUser == null
                || !samePersistentUser(principal.getUser(), requestedUser)) {
            throw new AccessDeniedException("A user may only purchase tickets for their own account");
        }
    }

    public void requireCanAccessUserEmail(String email) {
        Authentication authentication = currentAuthentication();
        UserPrincipal principal = principal(authentication);
        if (hasRole(authentication, "ADMIN")) {
            return;
        }
        User requestedUser = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (principal == null || requestedUser == null
                || !samePersistentUser(principal.getUser(), requestedUser)) {
            throw new AccessDeniedException("A user may only access their own tickets");
        }
    }

    public void requireCanAccessTicket(Ticket ticket) {
        requireTicketOwnerOrPrivileged(ticket, "STAFF", "ADMIN");
    }

    public void requireCanCancelTicket(Ticket ticket) {
        requireTicketOwnerOrPrivileged(ticket, "ADMIN");
    }

    private void requireTicketOwnerOrPrivileged(Ticket ticket, String... privilegedRoles) {
        Authentication authentication = currentAuthentication();
        UserPrincipal principal = principal(authentication);
        Long ownerId = ticket.getUser().getId();
        if (principal == null || !hasAnyRole(authentication, privilegedRoles)
                && !samePersistentUserId(principal.getUser().getId(), ownerId)) {
            throw new AccessDeniedException("A user may only access their own tickets");
        }
    }

    private boolean samePersistentUser(User first, User second) {
        return samePersistentUserId(first.getId(), second.getId());
    }

    private boolean samePersistentUserId(Long firstId, Long secondId) {
        return firstId != null && secondId != null && Objects.equals(firstId, secondId);
    }

    public void requireStaffOrAdmin() {
        Authentication authentication = currentAuthentication();
        if (!hasAnyRole(authentication, "STAFF", "ADMIN")) {
            throw new AccessDeniedException("STAFF or ADMIN role is required");
        }
    }

    public boolean isStaffOrAdmin(Authentication authentication) {
        return hasAnyRole(authentication, "STAFF", "ADMIN");
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private UserPrincipal principal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            return null;
        }
        return userPrincipal;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null
                && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        return Arrays.stream(roles).anyMatch(role -> hasRole(authentication, role));
    }
}
