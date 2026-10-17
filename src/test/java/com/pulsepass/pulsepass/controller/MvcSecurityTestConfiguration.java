package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.config.SecurityConfig;
import com.pulsepass.pulsepass.security.ApiAccessDeniedHandler;
import com.pulsepass.pulsepass.security.ApiAuthenticationEntryPoint;
import com.pulsepass.pulsepass.security.AuthorizationService;
import com.pulsepass.pulsepass.security.JwtAuthenticationFilter;
import com.pulsepass.pulsepass.security.SecurityRequestContextFilter;
import com.pulsepass.pulsepass.security.SecurityErrorResponseWriter;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.repository.UserCredentialRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import static org.mockito.Mockito.mock;

@TestConfiguration(proxyBeanMethods = false)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        ApiAuthenticationEntryPoint.class,
        ApiAccessDeniedHandler.class,
        SecurityErrorResponseWriter.class,
        SecurityRequestContextFilter.class,
        AuthorizationService.class
})
class MvcSecurityTestConfiguration {

    @Bean
    UserRepository securityTestUserRepository() {
        return mock(UserRepository.class);
    }

    @Bean
    UserCredentialRepository securityTestUserCredentialRepository() {
        return mock(UserCredentialRepository.class);
    }
}
