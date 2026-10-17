package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.dto.request.LoginRequest;
import com.pulsepass.pulsepass.dto.response.AuthResponse;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.security.SecurityRequestContextFilter;
import com.pulsepass.pulsepass.security.UserPrincipal;
import com.pulsepass.pulsepass.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal.getUser());
        LOGGER.info("event=auth.login.success requestId={}",
                MDC.get(SecurityRequestContextFilter.REQUEST_ID_MDC_KEY));

        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }
}
