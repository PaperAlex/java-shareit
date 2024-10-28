package ru.practicum.shareit.item.service;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto) throws NotFoundException;

    ItemDto getItemById(Long itemId) throws NotFoundException;

    ItemDto update(Long itemId, ItemDto newItemDto, Long ownerId) throws ValidationException, NotFoundException;

    void deleteItem(Long itemId) throws ValidationException, NotFoundException;

    Collection<ItemDto> getItemByUser(Long userId) throws NotFoundException;

    Collection<ItemDto> searchItemByText(String text);
}
