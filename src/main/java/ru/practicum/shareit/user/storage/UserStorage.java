package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user, Long id) throws ValidationException, NotFoundException;

    void deleteUser(Long id) throws ValidationException, NotFoundException;

    Optional<User> getUserById(Long id) throws NotFoundException;

    Collection<User> getUsers();

    void validateEmail(User user) throws DuplicatedDataException;
}

