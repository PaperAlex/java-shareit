package ru.practicum.shareit.user.service;

import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto userDto) throws DuplicatedDataException;

    UserDto update(UserDto userDto, Long userId) throws DuplicatedDataException, ValidationException, NotFoundException;

    void deleteUser(Long id) throws ValidationException, NotFoundException;

    Collection<UserDto> getUsers();

    UserDto getUserById(Long id) throws NotFoundException;


}

