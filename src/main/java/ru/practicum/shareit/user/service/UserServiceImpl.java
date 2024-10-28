package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) throws DuplicatedDataException {
        validateEmail(userDto);
        User user = userStorage.create(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(UserDto userDto, Long id) throws ValidationException, NotFoundException, DuplicatedDataException {
        validateEmail(userDto);
        User user = userStorage.update(UserMapper.toUser(userDto), id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Long id) throws ValidationException, NotFoundException {
        userStorage.deleteUser(id);
    }

    @Override
    public UserDto getUserById(Long id) throws NotFoundException {
        Optional<User> user = userStorage.getUserById(id);
        return UserMapper.toUserDto(user.get());
    }

    @Override
    public Collection<UserDto> getUsers() {
        return userStorage.getUsers().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    private void validateEmail(UserDto userDto) throws DuplicatedDataException {
        if (userStorage.getUsers().stream().anyMatch(user -> user.getEmail().equals(userDto.getEmail()))) {
            throw new DuplicatedDataException(String.format("email %s уже используется", userDto.getEmail()));
        }
    }
}
