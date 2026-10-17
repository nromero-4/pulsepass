package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.domain.UserCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    @EntityGraph(attributePaths = {"user.roles", "user.profile"})
    @Query("select credential from UserCredential credential where credential.user.username = :username")
    Optional<UserCredential> findWithUserByUsername(@Param("username") String username);

    @EntityGraph(attributePaths = {"user.roles", "user.profile"})
    @Query("select credential from UserCredential credential "
            + "where lower(credential.user.email) = lower(:email)")
    Optional<UserCredential> findWithUserByEmailIgnoreCase(@Param("email") String email);
}
