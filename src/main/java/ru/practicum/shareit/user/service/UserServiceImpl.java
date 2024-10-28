package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        userStorage.validateEmail(UserMapper.toUser(userDto));
        User user = userStorage.create(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(UserDto userDto, Long id) throws ValidationException, NotFoundException, DuplicatedDataException {
        userStorage.validateEmail(UserMapper.toUser(userDto));
        User user = userStorage.getUserById(id).get();
        User newUser = UserMapper.toUser(userDto);
        if (StringUtils.isNotBlank(newUser.getName())) {
            user.setName(newUser.getName());
        }
        if (StringUtils.isNotBlank(newUser.getEmail())) {
            user.setEmail(newUser.getEmail());
        }
        userStorage.update(user, id);
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
}
