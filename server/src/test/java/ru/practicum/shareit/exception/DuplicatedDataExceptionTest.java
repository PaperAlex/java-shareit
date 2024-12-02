package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DuplicatedDataExceptionTest {
    @Test
    public void testDuplicatedDataExceptionTestMessage() {
        String expectedMessage = "Not found";

        DuplicatedDataException exception = new DuplicatedDataException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
    }
}
