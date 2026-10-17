package com.pulsepass.pulsepass.dto.request;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank
        String username,
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 12, max = 72, message = "Password must contain between 12 and 72 characters")
        @BcryptPassword
        String password,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        String phone,
        String city,
        @NotNull
        LocalDate birthDate
) {
}
