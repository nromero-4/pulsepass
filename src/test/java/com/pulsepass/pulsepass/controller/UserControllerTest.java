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
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.UserService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

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
                .andExpect(jsonPath("$.email").value("andrea@example.com"));

        verify(userService).register(request);
    }

    @Test
    void registerRejectsInvalidEmailWithoutCallingService() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                "andrea", "not-an-email", "Andrea", "Lopez", null, null, LocalDate.of(2000, 1, 1));

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

        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("andrea@example.com"));

        verify(userService).findByEmail("andrea@example.com");
    }

    @Test
    void findByEmailReturnsNotFoundResponse() throws Exception {
        when(userService.findByEmail("missing@example.com"))
                .thenThrow(new ResourceNotFoundException("User not found: missing@example.com"));

        mockMvc.perform(get("/api/users/by-email").param("email", "missing@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found: missing@example.com"));

        verify(userService).findByEmail("missing@example.com");
    }

    @Test
    void findByUsernameReturnsUser() throws Exception {
        when(userService.findByUsername("andrea")).thenReturn(user());

        mockMvc.perform(get("/api/users/by-username").param("username", "andrea"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(userService).findByUsername("andrea");
    }

    private RegisterUserRequest validRequest() {
        return new RegisterUserRequest("andrea", "andrea@example.com", "Andrea", "Lopez",
                "3001234567", "Santa Marta", LocalDate.of(2000, 1, 1));
    }

    private UserResponse user() {
        return new UserResponse(1L, "andrea", "andrea@example.com", "Andrea", "Lopez",
                "3001234567", "Santa Marta", LocalDate.of(2000, 1, 1), true);
    }
}