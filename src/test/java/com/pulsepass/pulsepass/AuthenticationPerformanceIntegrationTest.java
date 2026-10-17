package com.pulsepass.pulsepass;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.pulsepass.dto.request.LoginRequest;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.security.TestJwtSecret;
import com.pulsepass.pulsepass.service.AuthService;
import com.pulsepass.pulsepass.service.UserService;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.hibernate.SessionFactory;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AuthenticationPerformanceIntegrationTest {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AuthenticationPerformanceIntegrationTest.class);
    private static final int WARMUP_ITERATIONS = 3;
    private static final int MEASURED_ITERATIONS = 20;

    @DynamicPropertySource
    static void registerTestJwtSecret(DynamicPropertyRegistry registry) {
        TestJwtSecret.register(registry);
        registry.add("spring.jpa.properties.hibernate.generate_statistics", () -> true);
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void measuresSequentialLoginLatencyAgainstPostgres() {
        String identifier = "perf-" + UUID.randomUUID();
        String password = "Performance-Passphrase-2026!";
        userService.register(new RegisterUserRequest(
                identifier, identifier + "@example.com", password,
                "Performance", "Test", null, null, LocalDate.of(1990, 1, 1)));
        LoginRequest request = new LoginRequest(identifier, password);

        for (int iteration = 0; iteration < WARMUP_ITERATIONS; iteration++) {
            assertThat(jwtService.isTokenValid(authService.login(request).accessToken())).isTrue();
        }

        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        long[] latencyNanos = new long[MEASURED_ITERATIONS];
        long benchmarkStarted = System.nanoTime();
        for (int iteration = 0; iteration < MEASURED_ITERATIONS; iteration++) {
            long started = System.nanoTime();
            var response = authService.login(request);
            latencyNanos[iteration] = System.nanoTime() - started;
            assertThat(jwtService.isTokenValid(response.accessToken())).isTrue();
        }
        long elapsedNanos = System.nanoTime() - benchmarkStarted;
        Arrays.sort(latencyNanos);

        double averageMillis = elapsedNanos / 1_000_000.0 / MEASURED_ITERATIONS;
        double p50Millis = percentileMillis(latencyNanos, 50);
        double p95Millis = percentileMillis(latencyNanos, 95);
        double throughputPerSecond = MEASURED_ITERATIONS / (elapsedNanos / 1_000_000_000.0);
        long databaseStatements = statistics.getPrepareStatementCount();
        double statementsPerLogin = databaseStatements / (double) MEASURED_ITERATIONS;
        LOGGER.info("Authentication benchmark: database=PostgreSQL, iterations={}, "
                        + "averageMs={}, p50Ms={}, p95Ms={}, throughputPerSecond={}, "
                        + "databaseStatements={}, statementsPerLogin={}",
                MEASURED_ITERATIONS, averageMillis, p50Millis, p95Millis, throughputPerSecond,
                databaseStatements, statementsPerLogin);

        assertThat(latencyNanos).hasSize(MEASURED_ITERATIONS);
        assertThat(databaseStatements)
                .as("database statements to load credentials, principal and roles")
                .isLessThanOrEqualTo(2L * MEASURED_ITERATIONS);
    }

    private double percentileMillis(long[] sortedLatencies, int percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * sortedLatencies.length) - 1;
        return sortedLatencies[index] / 1_000_000.0;
    }
}
