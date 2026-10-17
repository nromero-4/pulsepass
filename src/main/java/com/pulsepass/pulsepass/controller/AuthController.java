package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.dto.request.LoginRequest;
import com.pulsepass.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.pulsepass.dto.response.AuthResponse;
import com.pulsepass.pulsepass.security.UserPrincipal;
import com.pulsepass.pulsepass.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> currentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new AuthenticatedUserResponse(
                principal.getUser().getId(),
                principal.getUser().getUsername(),
                principal.getUser().getEmail(),
                principal.getUser().getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())));
    }
}
