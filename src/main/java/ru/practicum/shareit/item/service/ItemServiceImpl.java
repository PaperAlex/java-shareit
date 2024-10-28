package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemStorage itemStorage;

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) throws NotFoundException {
        userService.getUserById(ownerId);
        Item item = itemStorage.create(ItemMapper.toItem(itemDto), ownerId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) throws NotFoundException {
        Item item = itemStorage.getItemById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("id %s не найден", itemId)));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto newItemDto, Long ownerId) throws ValidationException, NotFoundException {
        userService.getUserById(ownerId);
        Item newItem = ItemMapper.toItem(newItemDto);
        Item item = itemStorage.getItemById(itemId).get();
        if (!item.getOwnerId().equals(ownerId)) {
            throw new ValidationException("Запрос от пользователя, не являющимся владельцем товара");
        }
        if (newItem.getName() != null) {
            item.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            item.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            item.setAvailable(newItem.getAvailable());
        }
        itemStorage.update(itemId, item, ownerId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public void deleteItem(Long itemId) throws ValidationException, NotFoundException {

    }

    @Override
    public Collection<ItemDto> getItemByUser(Long userId) throws NotFoundException {
        userService.getUserById(userId);
        return itemStorage.getItemByUser(userId).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());

    }

    @Override
    public Collection<ItemDto> searchItemByText(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        return itemStorage.searchItemByText(text).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }
}
