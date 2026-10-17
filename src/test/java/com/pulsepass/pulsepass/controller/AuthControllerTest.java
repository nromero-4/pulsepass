package com.pulsepass.pulsepass.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.dto.request.LoginRequest;
import com.pulsepass.pulsepass.dto.response.AuthResponse;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.security.UserPrincipal;
import com.pulsepass.pulsepass.service.AuthService;
import com.pulsepass.pulsepass.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@Import(MvcSecurityTestConfiguration.class)
@TestPropertySource(properties = "pulsepass.security.cors.allowed-origins=https://ui.example.test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loginReturnsAccessTokenAndExpiration() throws Exception {
        LoginRequest request = new LoginRequest("andrea@example.com", "Password123!");
        when(authService.login(request)).thenReturn(new AuthResponse("jwt-token", "Bearer", 900));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(authService).login(request);
    }

    @Test
    void invalidLoginReturnsGenericUnauthorizedWithoutSensitiveDetails() throws Exception {
        LoginRequest request = new LoginRequest("unknown@example.com", "WrongPassword123!");
        when(authService.login(request)).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    void currentUserReturnsOnlySafeIdentityFields() throws Exception {
        User user = new User("andrea", "andrea@example.com");
        ReflectionTestUtils.setField(user, "id", 7L);
        UserPrincipal principal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        mockMvc.perform(get("/api/auth/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void validBearerTokenLoadsCurrentPrincipalFromPersistence() throws Exception {
        UserPrincipal principal = principal(true);
        when(jwtService.isTokenValid("signed-token")).thenReturn(true);
        when(jwtService.extractUserId("signed-token")).thenReturn(7L);
        when(userDetailsService.loadUserById(7L)).thenReturn(principal);

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer signed-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(userDetailsService).loadUserById(7L);
    }

    @Test
    void invalidAndDisabledBearerTokensDoNotAuthenticate() throws Exception {
        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        when(jwtService.isTokenValid("disabled-token")).thenReturn(true);
        when(jwtService.extractUserId("disabled-token")).thenReturn(8L);
        when(userDetailsService.loadUserById(8L)).thenReturn(principal(8L, false));
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer disabled-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void duplicateBearerHeadersDoNotAuthenticate() throws Exception {
        when(jwtService.isTokenValid("signed-token")).thenReturn(true);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer signed-token", "Bearer other-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verify(userDetailsService, org.mockito.Mockito.never()).loadUserById(7L);
    }

    @Test
    void bearerHeaderCannotReplaceAnAlreadyEstablishedAuthentication() throws Exception {
        UserPrincipal principal = principal(true);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/auth/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7));

        verify(userDetailsService, org.mockito.Mockito.never()).loadUserById(7L);
    }

    @Test
    void requestIdIsReturnedAndInvalidRequestIdIsReplaced() throws Exception {
        String requestId = "60cb33a8-2cfb-45ec-9a85-0fc95b795c48";
        mockMvc.perform(get("/api/auth/me").header("X-Request-ID", requestId))
                .andExpect(status().isUnauthorized())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("X-Request-ID", requestId));

        mockMvc.perform(get("/api/auth/me").header("X-Request-ID", "not-a-uuid"))
                .andExpect(status().isUnauthorized())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().exists("X-Request-ID"));
    }

    @Test
    void corsAllowsOnlyConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/me")
                        .header("Origin", "https://ui.example.test")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://ui.example.test"));

        mockMvc.perform(options("/api/auth/me")
                        .header("Origin", "https://untrusted.example.test")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    private UserPrincipal principal(boolean active) {
        return principal(7L, active);
    }

    private UserPrincipal principal(Long userId, boolean active) {
        User user = new User("andrea", "andrea@example.com");
        ReflectionTestUtils.setField(user, "id", userId);
        user.setActive(active);
        return UserPrincipal.create(user);
    }
}
