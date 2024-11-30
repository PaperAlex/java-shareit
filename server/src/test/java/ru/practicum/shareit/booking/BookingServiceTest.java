package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.enums.Statuses;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    @InjectMocks
    private UserServiceImpl userService;

    private final User user = new User(1L, "User", "name@email.com");
    private final UserDto owner = new UserDto(1L, "name", "owner@email.com");
    private final User booker = new User(2L, "booker", "booker@mail.ru");
    private final UserDto bookerDto = new UserDto(2L, "bookerDto", "bookerDto@mail.ru");
    private final Item item = new Item(1L, "item", "cool", true, user, null);
    private final Booking booking = new Booking(1L,
            LocalDateTime.of(2023, 7, 1, 12, 12, 12),
            LocalDateTime.of(2023, 7, 30, 12, 12, 12),
            item, booker, Statuses.WAITING);
    private final BookingDto bookingDto = new BookingDto(
            LocalDateTime.of(2023, 7, 1, 12, 12, 12),
            LocalDateTime.of(2023, 7, 30, 12, 12, 12), 1L);
    private final BookingDto bookingDtoWrongItem = new BookingDto(
            LocalDateTime.of(2023, 7, 1, 12, 12, 12),
            LocalDateTime.of(2023, 7, 30, 12, 12, 12), 2L);
    private Long bookerId = 1L;

    @Test
    void testUserDoesNotExist() {
        when((userRepository).findById(3L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                bookingService.create(bookingDto, 3L));
    }

    @Test
    void testItemDoesNotExist() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when((itemRepository).findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                bookingService.create(bookingDtoWrongItem, 2L));
    }

    @Test
    void testItemsIsNotAvailable() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        item.setAvailable(false);

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.create(bookingDto, 2L));
    }

    @Test
    void testBookerIsOwnerOfItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.create(bookingDto, 1L));
    }

    @Test
    void testOwnerAttemptsToBookOwnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.create(bookingDto, 1L));
    }

    @Test
    void testApproveBooking() throws ValidationException, NotFoundException {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BookingDtoOut actualBooking = bookingService.approveBooking(1L, true, 1L);

        assertEquals(Statuses.APPROVED, actualBooking.getStatus());
    }

    @Test
    void testBookingDoesNotExist() {
        when((bookingRepository).findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                bookingService.approveBooking(2L, true, 1L));
    }

    @Test
    void testItemIsAlreadyBooked() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        booking.setStatus(Statuses.APPROVED);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, true, 1L));
    }

    @Test
    void testUserIsOwner() throws ValidationException, NotFoundException {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BookingDtoOut actualBooking = bookingService.getBookingById(1L, 1L);

        assertEquals(BookingMapper.toBookingDtoOut(booking), actualBooking);
    }

    @Test
    void testUserIsNotAuthorNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.getBookingById(1L, 3L));
    }

    @Test
    void testReturnAllBookings() throws ValidationException, NotFoundException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByBookerId("ALL", 2L);

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);
    }

    @Test
    void testReturnCurrentBookings() throws ValidationException, NotFoundException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllCurrentByBookerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByBookerId("CURRENT", 2L);

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);
    }

    @Test
    void testReturnPastBookings() throws ValidationException, NotFoundException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllPastByBookerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByBookerId("PAST", 2L);

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);
    }

    @Test
    void testReturnFutureBookings() throws ValidationException, NotFoundException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllFutureByBookerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByBookerId("FUTURE", 2L);

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);
    }

    @Test
    void testReturnWaitingBookings() throws ValidationException, NotFoundException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatus(anyLong(), any(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByBookerId("WAITING", 2L);

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);
    }

    @Test
    void testReturnAllBookingsForOwner() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "ALL");

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);

    }

    @Test
    void testReturnCurrentBookingsForOwner() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllCurrentByOwnerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "CURRENT");

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);
    }

    @Test
    void testReturnPastBookingsForOwner() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllPastByOwnerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "PAST");

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);
    }

    @Test
    void testReturnFutureBookingsForOwner() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllFutureByOwnerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "FUTURE");

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);
    }

    @Test
    void testReturnWaitingBookingsForOwner() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStatus(anyLong(), any(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "WAITING");

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);
    }

    @Test
    void testUserIsNotOwnerOfItem() {
        lenient().when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, true, 2L));
    }


    @Test
    void testBookingDoesNotExistInGetById() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                bookingService.getBookingById(1L, 2L));
    }

    @Test
    void testNoBookingsForBooker() throws ValidationException, NotFoundException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingDtoOut> actualBookings = bookingService.getAllByBookerId("ALL", 2L);

        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void testNoBookingsForOwner() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "ALL");

        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void testRejectBooking() throws ValidationException, NotFoundException {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BookingDtoOut actualBooking = bookingService.approveBooking(1L, false, 1L);

        assertEquals(Statuses.REJECTED, actualBooking.getStatus());
    }

    @Test
    void testGettingBookingWithInvalidId() {
        when(bookingRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                bookingService.getBookingById(2L, 1L));
    }

    @Test
    void testNoBookingsForOwnerInGetAllByOwner() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "ALL");

        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void testNoBookingsForBookerInGetAllByBooker() throws ValidationException, NotFoundException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingDtoOut> actualBookings = bookingService.getAllByBookerId("ALL", 2L);

        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void testItemIsNotAvailable() {
        User user = new User(2L, "Test User", "test@example.com");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        item.setAvailable(false);

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.create(bookingDto, 2L));
    }

    @Test
    void testApprovingBookingByNonOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, true, 2L));
    }

    @Test
    void testBookingIsNotPending() {
        booking.setStatus(Statuses.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, true, 1L));
    }

    @Test
    void testReturnAllRejectedBookingsForOwner() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStatus(anyLong(), any(), any())).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "REJECTED");

        assertEquals(List.of(BookingMapper.toBookingDtoOut(booking)), actualBookings);
    }

    @Test
    void testBookingIsNotFoundOnGetById() {
        when(bookingRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                bookingService.getBookingById(2L, 1L));
    }

    @Test
    void testNoBookingsForOwnerAndStateIsWaiting() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStatus(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "WAITING");

        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void testNoBookingsForBookerWithStateRejected() throws ValidationException, NotFoundException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatus(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        List<BookingDtoOut> actualBookings = bookingService.getAllByBookerId("REJECTED", 2L);
        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void testUserIsNotAvailable() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                bookingService.create(bookingDto, 2L));
    }

    @Test
    void testBookingAlreadyApproved() {
        booking.setStatus(Statuses.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, true, 1L));
    }

    @Test
    void testGettingBookingWithNegativeId() {
        Assertions.assertThrows(NotFoundException.class, () ->
                bookingService.getBookingById(-1L, 1L));
    }

    @Test
    void testNoBookingsForBookerWithStateWaiting() throws ValidationException, NotFoundException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatus(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        List<BookingDtoOut> actualBookings = bookingService.getAllByBookerId("WAITING", 2L);
        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void testNoBookingsForOwnerWithStateRejected() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStatus(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "REJECTED");
        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void testBookingStartDateIsInThePast() {
        bookingDto.setStart(LocalDateTime.now().minusDays(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.create(bookingDto, 2L));
    }

    @Test
    void testBookingEndDateIsBeforeStartDate() {
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.create(bookingDto, 2L));
    }

    @Test
    void testApprovingAlreadyApprovedBooking() {
        booking.setStatus(Statuses.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, true, 1L));
    }

    @Test
    void testRejectingAlreadyRejectedBooking() {
        booking.setStatus(Statuses.REJECTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, false, 1L));
    }

    @Test
    void testItemIsNotAvailableForBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        item.setAvailable(false);

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.create(bookingDto, 2L));
    }

    @Test
    void testGettingBookingByIdWithNegativeId() {
        Assertions.assertThrows(NotFoundException.class, () ->
                bookingService.getBookingById(-1L, 1L));
    }

    @Test
    void testNoBookingsForOwnerWithStateFuture() throws ValidationException, NotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllFutureByOwnerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingDtoOut> actualBookings = bookingService.getAllByOwnerId(1L, "FUTURE");
        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void testNoBookingsForBookerWithStateFuture() throws ValidationException, NotFoundException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllFutureByBookerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingDtoOut> actualBookings = bookingService.getAllByBookerId("FUTURE", 2L);
        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void testBookingIsNotPendingOnApprove() {
        booking.setStatus(Statuses.REJECTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, true, 1L));
    }

    @Test
    void testBookingIsNotPendingOnReject() {
        booking.setStatus(Statuses.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, false, 1L));
    }

    @Test
    void testApprovingBookingWithInvalidUser() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, true, 2L));
    }

    @Test
    void testBookingStartDateIsAfterEndDate() {
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.create(bookingDto, 2L));
    }

    @Test
    void testItemIsNotAvailableDuringBookingPeriod() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        LocalDateTime now = LocalDateTime.now();
        booking.setStart(now.plusDays(1));
        booking.setEnd(now.plusDays(3));
        item.setAvailable(false);

        Assertions.assertThrows(ValidationException.class, () ->
                bookingService.create(bookingDto, 2L));
    }

    @Test
    void testGetAllByBookerAll() throws ValidationException, NotFoundException {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(booker.getId(), Sort.by("start").descending()))
                .thenReturn(List.of(booking));


        List<BookingDtoOut> result = bookingService.getAllByBookerId("ALL", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllByBookerCurrent() throws ValidationException, NotFoundException {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllCurrentByBookerId(booker.getId(), Sort.by("start").descending()))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> result = bookingService.getAllByBookerId("CURRENT", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllByBookerPast() throws ValidationException, NotFoundException {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllPastByBookerId(booker.getId(), Sort.by("start").descending()))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> result = bookingService.getAllByBookerId("PAST", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllByBookerFuture() throws ValidationException, NotFoundException {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllFutureByBookerId(booker.getId(), Sort.by("start").descending()))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> result = bookingService.getAllByBookerId("FUTURE", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllByBookerWaiting() throws ValidationException, NotFoundException {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatus(booker.getId(), Statuses.WAITING, Sort.by("start").descending()))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> result = bookingService.getAllByBookerId("WAITING", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}