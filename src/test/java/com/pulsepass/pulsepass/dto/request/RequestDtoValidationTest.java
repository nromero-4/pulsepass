package com.pulsepass.pulsepass.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.pulsepass.enums.EventCategory;
import com.pulsepass.pulsepass.enums.TicketType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RequestDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void createEventRejectsMissingFieldsAndInvalidMinimumAgeAndDescriptionLength() {
        CreateEventRequest request = new CreateEventRequest(
                "", "", "x".repeat(1001), null, null, -1, "");

        Set<String> invalidFields = invalidFields(request);

        assertThat(invalidFields).contains(
                "eventCode", "name", "description", "category", "eventDate", "minimumAge", "venueCode");
    }

    @Test
    void registerUserRejectsMissingFieldsAndInvalidEmail() {
        RegisterUserRequest request = new RegisterUserRequest(
                "", "not-an-email", "", "", null, null, null);

        Set<String> invalidFields = invalidFields(request);

        assertThat(invalidFields).contains("username", "email", "firstName", "lastName", "birthDate");
    }

    @Test
    void purchaseTicketRejectsInvalidEmailAndMissingFields() {
        PurchaseTicketRequest request = new PurchaseTicketRequest("not-an-email", "", null);

        Set<String> invalidFields = invalidFields(request);

        assertThat(invalidFields).contains("userEmail", "eventCode", "type");
    }

    private Set<String> invalidFields(Object request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}