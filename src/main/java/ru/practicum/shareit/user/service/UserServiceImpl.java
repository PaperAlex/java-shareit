package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;


    @Transactional
    @Override
    public UserDto create(UserDto userDto) throws DuplicatedDataException {
        validateEmail(userDto);
        User user = userRepository.save(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public UserDto update(UserDto userDto, Long id) throws ValidationException, NotFoundException, DuplicatedDataException {
        validateEmail(userDto);
        User user = userRepository.findById(id).get();
        User newUser = UserMapper.toUser(userDto);
        if (StringUtils.isNotBlank(newUser.getName())) {
            user.setName(newUser.getName());
        }
        if (StringUtils.isNotBlank(newUser.getEmail())) {
            user.setEmail(newUser.getEmail());
        }
        return UserMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long id) throws ValidationException, NotFoundException {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(Long id) throws NotFoundException {
        Optional<User> user = userRepository.findById(id);
        return UserMapper.toUserDto(user.get());
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<UserDto> getUsers() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    private void validateEmail(UserDto userDto) throws DuplicatedDataException {
        Optional<User> user = userRepository.findByEmail(userDto.getEmail());
        if (user.isPresent()) {
            throw new DuplicatedDataException(String.format("email %s уже используется", userDto.getEmail()));
        }
    }
}

