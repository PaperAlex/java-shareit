package ru.practicum.shareit.booking;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */

@Data
@Builder
public class Booking {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
    /* booker пользователь, который осуществляет бронирование (userId) */
    private Long booker;
    /*
    status — статус бронирования.
    WAITING — новое бронирование, ожидает одобрения,
    APPROVED — бронирование подтверждено владельцем,
    REJECTED — бронирование отклонено владельцем,
    CANCELED — бронирование отменено создателем.
     */
    private BookingStatus status;

}
