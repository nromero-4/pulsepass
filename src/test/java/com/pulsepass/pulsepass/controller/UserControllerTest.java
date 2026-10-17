package com.pulsepass.pulsepass.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.security.UserPrincipal;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.service.UserService;
import com.pulsepass.pulsepass.service.impl.UserDetailsServiceImpl;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

@WebMvcTest(UserController.class)
@Import(MvcSecurityTestConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerReturnsCreatedUser() throws Exception {
        RegisterUserRequest request = validRequest();
        when(userService.register(request)).thenReturn(user());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(userService).register(request);
    }

    @Test
    void registerRejectsInvalidEmailWithoutCallingService() throws Exception {
        RegisterUserRequest request = objectMapper.readValue("""
                {
                  "username": "andrea",
                  "email": "not-an-email",
                  "firstName": "Andrea",
                  "lastName": "Lopez",
                  "birthDate": "2000-01-01"
                }
                """, RegisterUserRequest.class);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.email").exists());

        verify(userService, never()).register(any(RegisterUserRequest.class));
    }

    @Test
    void registerReturnsConflictWhenUsernameAlreadyExists() throws Exception {
        RegisterUserRequest request = validRequest();
        when(userService.register(request))
                .thenThrow(new DuplicateResourceException("Username already exists: andrea"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username already exists: andrea"));

        verify(userService).register(request);
    }

    @Test
    void findByEmailReturnsUser() throws Exception {
        when(userService.findByEmail("andrea@example.com")).thenReturn(user());

        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@example.com")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("andrea@example.com"));

        verify(userService).findByEmail("andrea@example.com");
    }

    @Test
    void findByEmailReturnsNotFoundResponse() throws Exception {
        when(userService.findByEmail("missing@example.com"))
                .thenThrow(new ResourceNotFoundException("User not found: missing@example.com"));

        mockMvc.perform(get("/api/users/by-email").param("email", "missing@example.com")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found: missing@example.com"));

        verify(userService).findByEmail("missing@example.com");
    }

    @Test
    void findByUsernameReturnsUser() throws Exception {
        when(userService.findByUsername("andrea")).thenReturn(user());

        mockMvc.perform(get("/api/users/by-username").param("username", "andrea")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(userService).findByUsername("andrea");
    }

    @Test
    void userCanSearchOwnDataByPersistentIdentityOnly() throws Exception {
        User owner = new User("andrea", "andrea@example.com");
        ReflectionTestUtils.setField(owner, "id", 7L);
        UserPrincipal principal = UserPrincipal.create(owner);
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        when(userRepository.findByEmailIgnoreCase("andrea@example.com")).thenReturn(java.util.Optional.of(owner));
        when(userRepository.findByUsername("andrea")).thenReturn(java.util.Optional.of(owner));
        when(userService.findByEmail("andrea@example.com")).thenReturn(user());
        when(userService.findByUsername("andrea")).thenReturn(user());

        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@example.com")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users/by-username").param("username", "andrea")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotSearchAnotherAccountsPrivateData() throws Exception {
        User owner = new User("andrea", "andrea@example.com");
        ReflectionTestUtils.setField(owner, "id", 7L);
        User other = new User("carlos", "carlos@example.com");
        ReflectionTestUtils.setField(other, "id", 8L);
        UserPrincipal principal = UserPrincipal.create(owner);
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        when(userRepository.findByEmailIgnoreCase("carlos@example.com")).thenReturn(java.util.Optional.of(other));
        when(userRepository.findByUsername("carlos")).thenReturn(java.util.Optional.of(other));

        mockMvc.perform(get("/api/users/by-email").param("email", "carlos@example.com")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(get("/api/users/by-username").param("username", "carlos")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(userService, never()).findByEmail("carlos@example.com");
        verify(userService, never()).findByUsername("carlos");
    }

                private RegisterUserRequest validRequest() throws Exception {
                                return objectMapper.readValue("""
                                                                {
                                                                        "username": "andrea",
                                                                        "email": "andrea@example.com",
                                                                        "password": "Password123!",
                                                                        "firstName": "Andrea",
                                                                        "lastName": "Lopez",
                                                                        "phoneNumber": "3001234567",
                                                                        "address": "Santa Marta",
                                                                        "birthDate": "2000-01-01"
                                                                }
                                                                """, RegisterUserRequest.class);
    }

    private UserResponse user() {
        return new UserResponse(1L, "andrea", "andrea@example.com", "Andrea", "Lopez",
                "3001234567", "Santa Marta", LocalDate.of(2000, 1, 1), true);
    }
}