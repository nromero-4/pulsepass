package com.pulsepass.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.dto.request.LoginRequest;
import com.pulsepass.pulsepass.dto.response.AuthResponse;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.security.UserPrincipal;
import com.pulsepass.pulsepass.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void authenticatesIdentifierAndReturnsBearerToken() {
        LoginRequest request = new LoginRequest("andrea@example.com", "Password123!");
        User user = new User("andrea", "andrea@example.com");
        ReflectionTestUtils.setField(user, "id", 7L);
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authenticated = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticated);
        when(jwtService.generateToken(user)).thenReturn("signed-token");
        when(jwtService.getExpirationSeconds()).thenReturn(900L);

        AuthResponse response = authService.login(request);

        assertThat(response).isEqualTo(new AuthResponse("signed-token", "Bearer", 900L));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(user);
    }

    @Test
    void invalidCredentialsDoNotIssueAToken() {
        LoginRequest request = new LoginRequest("missing@example.com", "wrong-password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any(User.class));
    }
}
