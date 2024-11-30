package ru.practicum.shareit.booking.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.enums.States;
import ru.practicum.shareit.enums.Statuses;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public BookingDtoOut create(BookingDto bookingDto, Long userId) throws ValidationException, NotFoundException {
        User booker = getUser(userId);
        Item item = getItem(bookingDto.getItemId());
        if (!item.getAvailable()) {
            throw new ValidationException("Товар не доступен");
        }
        if (booker.getId().equals(item.getOwner().getId())) {
            throw new ValidationException("Не возможно бронировать свой товар");
        }
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart()) ||
                bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата бронирования не может быть раньше даты возврата");
        }
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        bookingRepository.save(BookingMapper.toBooking(bookingDto, booking));
        log.info("Бронирование с идентификатором {} создано", booking.getId());
        return BookingMapper.toBookingDtoOut(booking);
    }

    @Transactional
    @Override
    public BookingDtoOut approveBooking(Long bookingId, Boolean isApproved, Long userId) throws ValidationException, NotFoundException {
        Booking booking = getById(bookingId);
        Item item = getItem(booking.getItem().getId());

        if (booking.getStatus() != Statuses.WAITING) {
            throw new ValidationException("Товар уже забронирован");
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Бронь может подтврдить только собственник");
        }
        Statuses newBookingStatus = isApproved ? Statuses.APPROVED : Statuses.REJECTED;
        booking.setStatus(newBookingStatus);
        return BookingMapper.toBookingDtoOut(booking);

    }

    @Transactional(readOnly = true)
    @Override
    public BookingDtoOut getBookingById(Long bookingId, Long userId) throws ValidationException, NotFoundException {
        Booking booking = getById(bookingId);
        User booker = booking.getBooker();
        User owner = getUser(booking.getItem().getOwner().getId());
        if (!booker.getId().equals(userId) && !owner.getId().equals(userId)) {
            throw new ValidationException("Только создатель брони или владелец может просматривать броинрование");
        }
        return BookingMapper.toBookingDtoOut(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDtoOut> getAllByBookerId(String state, Long bookerId) throws NotFoundException, ValidationException {
        States bookingState = States.valueOf(state);
        User booker = getUser(bookerId);
        Collection<Booking> bookings;

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findAllByBookerId(booker.getId(), Sort.by(DESC, "start"));
                break;
            case CURRENT:
                bookings = bookingRepository.findAllCurrentByBookerId(booker.getId(),
                        Sort.by(Sort.Direction.DESC, "start"));
                break;
            case PAST:
                bookings = bookingRepository.findAllPastByBookerId(booker.getId(),
                        Sort.by(Sort.Direction.DESC, "start"));
                break;
            case FUTURE:
                bookings = bookingRepository.findAllFutureByBookerId(booker.getId(),
                        Sort.by(Sort.Direction.DESC, "start"));
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatus(booker.getId(),
                        Statuses.WAITING, Sort.by(Sort.Direction.DESC, "start"));
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatus(booker.getId(),
                        Statuses.REJECTED, Sort.by(DESC, "end"));
                break;
            default:
                throw new ValidationException("Неизвестный параметр");
        }
        return bookings.stream().map(BookingMapper::toBookingDtoOut).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDtoOut> getAllByOwnerId(Long ownerId, String state) throws NotFoundException, ValidationException {
        States bookingState = States.valueOf(state);
        User owner = getUser(ownerId);
        Collection<Booking> bookings;

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findAllByOwnerId(owner.getId(),
                        Sort.by(Sort.Direction.DESC, "start"));
                break;
            case CURRENT:
                bookings = bookingRepository.findAllCurrentByOwnerId(owner.getId(),
                        Sort.by(Sort.Direction.DESC, "start"));
                break;
            case PAST:
                bookings = bookingRepository.findAllPastByOwnerId(owner.getId(),
                        Sort.by(Sort.Direction.DESC, "start"));
                break;
            case FUTURE:
                bookings = bookingRepository.findAllFutureByOwnerId(owner.getId(),
                        Sort.by(Sort.Direction.DESC, "start"));
                break;
            case WAITING:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(owner.getId(),
                        Statuses.WAITING, Sort.by(Sort.Direction.DESC, "start"));
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(owner.getId(),
                        Statuses.REJECTED, Sort.by(Sort.Direction.DESC, "start"));
                break;
            default:
                throw new ValidationException("Неизвестный параметр");
        }
        return bookings.stream().map(BookingMapper::toBookingDtoOut).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Booking getById(Long bookingId) throws NotFoundException {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь %d не найден", bookingId)));
    }

    private User getUser(Long userId) throws NotFoundException {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь %d не найден", userId)));
    }

    private Item getItem(Long itemId) throws NotFoundException {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item %d не найден", itemId)));
    }
}