package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidationExceptionTest {
    @Test
    public void testValidationExceptionMessage() {
        String expectedMessage = "Ошибка валидации";

        ValidationException exception = new ValidationException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
    }
}

