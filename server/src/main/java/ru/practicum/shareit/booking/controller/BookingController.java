package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@RequiredArgsConstructor
@RestController("ServerBookingController")
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOut saveNewBooking(@RequestBody BookingDto bookingDtoIn,
                                        @RequestHeader("X-Sharer-User-Id") Long userId) throws ValidationException, NotFoundException {
        return bookingService.create(bookingDtoIn, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOut approve(@PathVariable long bookingId, @RequestParam(name = "approved") Boolean isApproved,
                                 @RequestHeader("X-Sharer-User-Id") long userId) throws ValidationException, NotFoundException {
        return bookingService.approveBooking(bookingId, isApproved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOut getBookingById(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId) throws ValidationException, NotFoundException {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDtoOut> getAllByBookerId(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                @RequestHeader("X-Sharer-User-Id") long bookerId) throws ValidationException, NotFoundException {
        return bookingService.getAllByBookerId(state, bookerId);
    }

    @GetMapping("/owner")
    public List<BookingDtoOut> getAllByOwnerId(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                               @RequestHeader("X-Sharer-User-Id") long ownerId) throws ValidationException, NotFoundException {
        return bookingService.getAllByOwnerId(ownerId, state);
    }
}
