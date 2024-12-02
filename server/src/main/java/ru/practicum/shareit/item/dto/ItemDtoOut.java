package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.item.comment.dto.CommentDtoOut;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemDtoOut {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDtoOut lastBooking;
    private BookingDtoOut nextBooking;
    private List<CommentDtoOut> comments;
    private UserDto owner;
    private Long requestId;

    public ItemDtoOut(Long id, String name, String description, Boolean available, UserDto owner) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.owner = owner;
    }

    public ItemDtoOut(Long id, String name, String description, Boolean available, UserDto owner, Long requestId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.owner = owner;
        this.requestId = requestId;
    }
}
