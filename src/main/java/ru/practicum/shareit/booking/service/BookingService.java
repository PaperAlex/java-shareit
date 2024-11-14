package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

public interface BookingService {
    BookingDtoOut create(BookingDto bookingDto, Long userId) throws ValidationException, NotFoundException;

    BookingDtoOut approveBooking(Long bookingId, Boolean isApproved, Long userId) throws ValidationException, NotFoundException;

    BookingDtoOut getBookingById(Long bookingId, Long userId) throws ValidationException, NotFoundException;

    List<BookingDtoOut> getAllByBookerId(String subState, Long bookerId) throws NotFoundException, ValidationException;

    List<BookingDtoOut> getAllByOwnerId(Long ownerId, String state) throws NotFoundException, ValidationException;
}

