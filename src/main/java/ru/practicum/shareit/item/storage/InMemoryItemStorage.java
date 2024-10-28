package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository("InMemoryItems")
@Slf4j
public class InMemoryItemStorage implements ItemStorage {
    Map<Long, Item> items = new HashMap<>();

    @Override
    public Item create(Item newItem, Long ownerId) {
        newItem.setId(generateId());
        newItem.setOwnerId(ownerId);
        items.put(newItem.getId(), newItem);
        log.info("Добавлен новый Item {}", newItem);
        return newItem;

    }

    @Override
    public Optional<Item> getItemById(Long itemId) throws NotFoundException {
        if (!items.containsKey(itemId)) {
            log.warn("Введен не верный id - {}", itemId);
            throw new NotFoundException("Item с таким ID не найден!");
        }
        log.debug("Запрос на Item номер - {}", itemId);
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Item update(Long itemId, Item newItem) throws ValidationException, NotFoundException {
        Item item = items.get(itemId);

        if (newItem.getName() != null) {
            item.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            item.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            item.setAvailable(newItem.getAvailable());
        }
        log.info("Успешно обновлен Item - {}", itemId);
        return newItem;
    }

    @Override
    public void deleteItem(Long itemId) throws ValidationException, NotFoundException {
        log.info("Удален item {}", itemId);
        if (itemId == null) {
            log.warn("Ошибка валидации : не указан id");
            throw new ValidationException("Ошибка валидации : Не указан id");
        }
        if (!items.containsKey(itemId)) {
            log.warn("Ошибка валидации : указан некорректный id");
            throw new NotFoundException("Ошибка валидации : указан некорректный id");
        }
        items.remove(itemId);
    }

    @Override
    public Collection<Item> getItemByUser(Long userId) {
        return items.values().stream().filter((item -> item.getOwnerId().equals(userId))).toList();
    }

    @Override
    public Collection<Item> searchItemByText(String text) {
        return items.values().stream().filter(item -> item.getAvailable().equals(true)
                && ((item.getName().toLowerCase().contains(text.toLowerCase())
                || item.getDescription().toLowerCase().contains(text.toLowerCase())))).collect(Collectors.toList());
    }

    private long generateId() {
        long id = items.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
        log.debug("Новый id - {}", id);
        return ++id;
    }
}
