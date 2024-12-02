package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotFoundExceptionTest {
    @Test
    public void testNotFoundExceptionMessage() {
        String expectedMessage = "Not found";

        NotFoundException exception = new NotFoundException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
    }
}