package com.pulsepass.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserCredential;
import com.pulsepass.pulsepass.enums.RoleName;
import com.pulsepass.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.service.impl.UserDetailsServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialRepository credentialRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadsUsernameAndPasswordHashFromSeparateCredential() {
        User user = user(7L, RoleName.USER);
        UserCredential credential = new UserCredential(user, "$2a$12$stored-hash");
        when(credentialRepository.findWithUserByUsername("andrea")).thenReturn(Optional.of(credential));

        var details = userDetailsService.loadUserByUsername("andrea");

        assertThat(details.getUsername()).isEqualTo("andrea");
        assertThat(details.getPassword()).isEqualTo("$2a$12$stored-hash");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void loadsByEmailAndReflectsCurrentRolesAndActiveStatus() {
        User user = user(8L, RoleName.STAFF);
        user.setActive(false);
        UserCredential credential = new UserCredential(user, "$2a$12$stored-hash");
        when(credentialRepository.findWithUserByUsername("staff@example.com")).thenReturn(Optional.empty());
        when(credentialRepository.findWithUserByEmailIgnoreCase("staff@example.com"))
                .thenReturn(Optional.of(credential));

        var details = userDetailsService.loadUserByUsername("staff@example.com");

        assertThat(details.isEnabled()).isFalse();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_STAFF");
    }

    @Test
    void legacyUserWithoutCredentialCannotAuthenticate() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("legacy"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    private User user(Long id, RoleName role) {
        User user = new User("andrea", "andrea@example.com");
        ReflectionTestUtils.setField(user, "id", id);
        user.getRoles().clear();
        user.addRole(role);
        return user;
    }
}
