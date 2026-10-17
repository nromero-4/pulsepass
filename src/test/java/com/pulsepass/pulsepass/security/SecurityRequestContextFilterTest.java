package com.pulsepass.pulsepass.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

class SecurityRequestContextFilterTest {

    @Test
    void rejectsInsecureRequestsOutsideLocalhostWhenHttpsIsRequired() throws Exception {
        SecurityRequestContextFilter filter = filter(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/events/published");
        request.setServerName("pulsepass.example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("HTTPS is required");
        assertThat(response.getHeader(SecurityRequestContextFilter.REQUEST_ID_HEADER)).isNotBlank();
    }

    @Test
    void allowsLocalhostForDevelopmentWhenHttpsIsRequired() throws Exception {
        SecurityRequestContextFilter filter = filter(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/events/published");
        request.setServerName("localhost");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void hostHeaderCannotBypassHttpsRequirementFromRemoteAddress() throws Exception {
        SecurityRequestContextFilter filter = filter(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/events/published");
        request.setServerName("localhost");
        request.setRemoteAddr("192.0.2.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void echoesOnlyAValidUuidRequestId() throws Exception {
        SecurityRequestContextFilter filter = filter(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/events/published");
        request.addHeader(SecurityRequestContextFilter.REQUEST_ID_HEADER, "not-a-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(SecurityRequestContextFilter.REQUEST_ID_HEADER))
                .matches("[0-9a-f-]{36}");
    }

    private SecurityRequestContextFilter filter(boolean requireHttps) {
        Environment environment = new MockEnvironment()
                .withProperty("pulsepass.security.require-https", Boolean.toString(requireHttps));
        return new SecurityRequestContextFilter(
                new SecurityErrorResponseWriter(new ObjectMapper()), environment);
    }
}
