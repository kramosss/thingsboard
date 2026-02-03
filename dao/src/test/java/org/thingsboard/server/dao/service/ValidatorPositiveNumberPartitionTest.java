package org.thingsboard.server.dao.service;

import org.junit.jupiter.api.Test;
import org.thingsboard.server.dao.exception.IncorrectParameterException;



import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;


class ValidatorPositiveNumberPartitionTest {
    @Test
    void validatePositiveNumber_MinValue_ShouldThrowException() {
        assertThatThrownBy(() -> Validator.validatePositiveNumber(Long.MIN_VALUE, "Value must be positive"))
                .as("Extreme boundary: Long.MIN_VALUE")
                .isInstanceOf(IncorrectParameterException.class)
                .hasMessageContaining("Value must be positive");
    }

    @Test
    void validatePositiveNumber_NegativeRepresentative_ShouldThrowException() {
        assertThatThrownBy(() -> Validator.validatePositiveNumber(-10, "Value must be positive"))
                .as("Negative representative value (-10)")
                .isInstanceOf(IncorrectParameterException.class)
                .hasMessageContaining("Value must be positive");
    }

    @Test
    void validatePositiveNumber_NegativeOne_ShouldThrowException() {
        assertThatThrownBy(() -> Validator.validatePositiveNumber(-1, "Value must be positive"))
                .as("Boundary value: -1 (closest to zero)")
                .isInstanceOf(IncorrectParameterException.class)
                .hasMessageContaining("Value must be positive");
    }

    @Test
    void validatePositiveNumber_Zero_ShouldThrowException() {
        assertThatThrownBy(() -> Validator.validatePositiveNumber(0, "Value must be positive"))
                .as("Critical boundary: zero (val <= 0)")
                .isInstanceOf(IncorrectParameterException.class)
                .hasMessageContaining("Value must be positive");
    }

    @Test
    void validatePositiveNumber_One_ShouldPass() {
        assertThatCode(() -> Validator.validatePositiveNumber(1, "Value must be positive"))
                .as("Boundary value: 1 (smallest positive)")
                .doesNotThrowAnyException();
    }

    @Test
    void validatePositiveNumber_PositiveRepresentative_ShouldPass() {
        assertThatCode(() -> Validator.validatePositiveNumber(10, "Value must be positive"))
                .as("Representative value: 10 (typical positive)")
                .doesNotThrowAnyException();
    }

    @Test
    void validatePositiveNumber_MaxValue_ShouldPass() {
        assertThatCode(() -> Validator.validatePositiveNumber(Long.MAX_VALUE, "Value must be positive"))
                .as("Extreme boundary: Long.MAX_VALUE")
                .doesNotThrowAnyException();
    }
}