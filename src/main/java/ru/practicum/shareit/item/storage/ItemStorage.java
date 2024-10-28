package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {
    Item create(Item item, Long ownerId);

    Optional<Item> getItemById(Long itemId) throws NotFoundException;

    Item update(Long itemId, Item newItem) throws ValidationException, NotFoundException;

    void deleteItem(Long itemId) throws ValidationException, NotFoundException;

    Collection<Item> getItemByUser(Long userId);

    Collection<Item> searchItemByText(String text);


}
