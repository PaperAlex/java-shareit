package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class BookingDtoTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testJsonBookingDto() throws Exception {
        BookingDto bookingDto = new BookingDto(
                LocalDateTime.of(2024, 11, 19, 11, 11, 11),
                LocalDateTime.of(2024, 11, 19, 11, 11, 12),
                1L);

        String json = objectMapper.writeValueAsString(bookingDto);
        BookingDto deserializedDto = objectMapper.readValue(json, BookingDto.class);

        assertEquals(bookingDto.getItemId(), deserializedDto.getItemId());
        assertEquals(bookingDto.getEnd(), deserializedDto.getEnd());
        assertEquals(bookingDto.getStart(), deserializedDto.getStart());
    }
}