package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository("InMemoryUsers")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    public User create(User newUser) {
        newUser.setId(generateId());
        users.put(newUser.getId(), newUser);
        log.info("Добавлен новый пользователь {}", newUser);
        return newUser;

    }

    public User update(User newUser, Long userId) throws ValidationException, NotFoundException {
        users.put(userId, newUser);
        log.info("Пользователь № {} успешно обновлен", newUser);
        return newUser;
    }

    public void deleteUser(Long userId) throws ValidationException, NotFoundException {
        log.info("Удален юзер {}", userId);
        if (userId == null) {
            log.warn("Ошибка валидации : не указан id");
            throw new ValidationException("Ошибка валидации : Не указан id");
        }
        if (!users.containsKey(userId)) {
            log.warn("Ошибка валидации : указан некорректный id");
            throw new NotFoundException("Ошибка валидации : указан некорректный id");
        }
        users.remove(userId);
    }

    public Optional<User> getUserById(Long userId) throws NotFoundException {
        if (!users.containsKey(userId)) {
            log.warn("Введен не верный id - {}", userId);
            throw new NotFoundException("Пользователь с таким ID не найден!");
        }
        log.debug("Запрос на пользователя номер - {}", userId);
        return Optional.ofNullable(users.get(userId));
    }

    public Collection<User> getUsers() {
        log.debug("Запрос на список всех пользователей : {}", users.values());
        return new ArrayList<>(users.values());
    }

    private long generateId() {
        long id = users.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
        log.debug("Новый id - {}", id);
        return ++id;
    }

    public void validateEmail(User userValidation) throws DuplicatedDataException {
        if (getUsers().stream().anyMatch(user -> user.getEmail().equals(userValidation.getEmail()))) {
            throw new DuplicatedDataException(String.format("email %s уже используется", userValidation.getEmail()));
        }
    }
}
