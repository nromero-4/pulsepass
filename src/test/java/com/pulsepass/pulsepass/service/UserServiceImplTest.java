package com.pulsepass.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.mapper.UserMapper;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.service.impl.UserServiceImpl;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerSavesActiveUserWithLinkedProfile() {
        RegisterUserRequest request = request(LocalDate.now().minusYears(25));
        UserResponse response = response();
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(response);

        UserResponse result = userService.register(request);

        assertThat(result).isSameAs(response);
        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getActive()).isTrue();
        assertThat(savedUser.getProfile()).isNotNull();
        assertThat(savedUser.getProfile().getUser()).isSameAs(savedUser);
        assertThat(savedUser.getProfile().getBirthDate()).isEqualTo(request.birthDate());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("andrea")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request(LocalDate.now().minusYears(25))))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("andrea");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmailIgnoringCase() {
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request(LocalDate.now().minusYears(25))))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("andrea@email.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerRejectsFutureBirthDate() {
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.register(request(LocalDate.now().plusDays(1))))
                .isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByEmailIgnoresCase() {
        User user = new User("andrea", "andrea@email.com");
        UserResponse response = response();
        when(userRepository.findByEmailIgnoreCase("ANDREA@EMAIL.COM")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        assertThat(userService.findByEmail("ANDREA@EMAIL.COM")).isSameAs(response);
    }

    @Test
    void findByUsernameReturnsMappedUser() {
        User user = new User("andrea", "andrea@email.com");
        UserResponse response = response();
        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        assertThat(userService.findByUsername("andrea")).isSameAs(response);
    }

    @Test
    void findByUsernameThrowsWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("missing"))
                .isInstanceOf(com.pulsepass.pulsepass.exception.ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }

    private RegisterUserRequest request(LocalDate birthDate) {
        return new RegisterUserRequest("andrea", "andrea@email.com", "Andrea", "Rojas",
                "3001234567", "Santa Marta", birthDate);
    }

    private UserResponse response() {
        return new UserResponse(1L, "andrea", "andrea@email.com", "Andrea", "Rojas",
                "3001234567", "Santa Marta", LocalDate.now().minusYears(25), true);
    }
}