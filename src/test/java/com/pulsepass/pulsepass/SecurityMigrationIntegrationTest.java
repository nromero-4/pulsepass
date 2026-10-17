package com.pulsepass.pulsepass;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class SecurityMigrationIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16"));

    @Test
    void upgradesLegacyUsersWithoutCreatingPasswordsOrPrivilegedRoles() throws Exception {
        flyway(MigrationVersion.fromVersion("4")).migrate();
        try (Connection connection = connection();
             PreparedStatement insertUser = connection.prepareStatement(
                     "INSERT INTO users (username, email, active) VALUES (?, ?, TRUE)")) {
            insertUser.setString(1, "legacy-user");
            insertUser.setString(2, "legacy@example.com");
            insertUser.executeUpdate();
        }

        flyway(null).migrate();
        flyway(null).migrate();

        try (Connection connection = connection();
             PreparedStatement findRole = connection.prepareStatement(
                     "SELECT role FROM user_roles "
                             + "JOIN users ON users.id = user_roles.user_id "
                             + "WHERE users.username = ?")) {
            findRole.setString(1, "legacy-user");
            try (var roles = findRole.executeQuery()) {
                assertThat(roles.next()).isTrue();
                assertThat(roles.getString("role")).isEqualTo("USER");
                assertThat(roles.next()).isFalse();
            }
        }
        try (Connection connection = connection();
             PreparedStatement findCredential = connection.prepareStatement(
                     "SELECT COUNT(*) FROM user_credentials")) {
            try (var credentials = findCredential.executeQuery()) {
                assertThat(credentials.next()).isTrue();
                assertThat(credentials.getInt(1)).isZero();
            }
        }
    }

    private Flyway flyway(MigrationVersion target) {
        var configuration = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration");
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    private Connection connection() throws Exception {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }
}
