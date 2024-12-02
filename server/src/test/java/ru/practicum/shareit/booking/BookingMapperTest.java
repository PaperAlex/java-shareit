package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.enums.Statuses;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class BookingMapperTest {
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime nextDay = LocalDateTime.now().plusDays(1);

    private final User user = new User(1L, "name", "name@email.com");
    private final Item item = new Item(1L, "name", "description",
            Boolean.TRUE, user, new ItemRequest("des"));

    private final UserDto userDto = new UserDto(1L, "name", "name@email.com");
    private final ItemDtoOut itemDtoOut = new ItemDtoOut(1L, "name", "description", true,
            userDto, 0L);
    private final BookingDtoOut newBooking = new BookingDtoOut(
            1L, now, nextDay, itemDtoOut, userDto, Statuses.WAITING);
    private final BookingDto dto = new BookingDto(
            LocalDateTime.of(2024, 11, 19, 11, 11, 11),
            LocalDateTime.of(2024, 11, 19, 11, 11, 12),
            1L);
    private final Booking booking = new Booking(1L, now, nextDay, item, user, Statuses.WAITING);

    @Test
    public void toBookingDtoOutTest() {
        BookingDtoOut bookingDtoOut = BookingMapper.toBookingDtoOut(booking);
        assertThat(bookingDtoOut, equalTo(newBooking));
    }

    @Test
    public void toBookingTest() {
        Booking b = BookingMapper.toBooking(dto, booking);
        assertThat(b.getStart(), equalTo(booking.getStart()));
        assertThat(b.getEnd(), equalTo(booking.getEnd()));
        assertThat(b.getStatus(), equalTo(booking.getStatus()));
        assertThat(b.getItem(), equalTo(item));
        assertThat(b.getBooker(), equalTo(user));
    }
}