package ru.practicum.shareit.item.service;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOut;

import java.util.Collection;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto) throws NotFoundException;

    ItemDtoOut getItemById(Long itemId, Long userId) throws NotFoundException;

    ItemDto update(Long itemId, ItemDto newItemDto, Long ownerId) throws ValidationException, NotFoundException;

    void deleteItem(Long itemId) throws ValidationException, NotFoundException;

    Collection<ItemDto> getItemByUser(Long userId) throws NotFoundException;

    Collection<ItemDto> searchItemByText(String text);

    CommentDtoOut addComment(Long itemId, CommentDto commentDto, Long userId) throws ValidationException, NotFoundException;

}
