package ru.practicum.shareit.booking;

import jakarta.validation.ValidationException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;


public class BookingClient extends BaseClient {
    public BookingClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> saveBooking(BookItemRequestDto bookItemRequestDto, long userId) {
        if (!bookItemRequestDto.getEnd().isAfter(bookItemRequestDto.getStart()) ||
                bookItemRequestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала бронирования должна быть раньше даты возврата");
        }
        return post("", userId, bookItemRequestDto);
    }

    public ResponseEntity<Object> approve(long bookingId, Boolean isApproved, long userId) {
        Map<String, Object> parameters = Map.of("approved", isApproved);
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getBookingById(long bookingId, long userId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllByBooker(Integer from, Integer size, BookingState state, long userId) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getAllByOwner(Integer from, Integer size, BookingState state, long userId) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }
}