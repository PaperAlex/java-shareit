package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.model.ItemRequest;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestDto requestDtoIn) {
        return new ItemRequest(
                requestDtoIn.getDescription()
        );
    }

    public static ItemRequestDtoOut toItemRequestDtoOut(ItemRequest request) {
        return new ItemRequestDtoOut(
                request.getId(),
                request.getDescription(),
                request.getRequestor().getId(),
                request.getCreated()
        );
    }
}