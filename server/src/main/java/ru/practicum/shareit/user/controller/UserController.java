package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@RestController("ServerUserController")
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) throws NotFoundException {
        return userService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserDto userDto) throws DuplicatedDataException, NotFoundException {
        return userService.create(userDto);
    }


    @PatchMapping("/{userId}")
    public UserDto updateUserById(@RequestBody UserDto userDto, @PathVariable Long userId)
            throws ValidationException, NotFoundException, DuplicatedDataException {
        UserDto update = userService.update(userDto, userId);
        return update;
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) throws ValidationException, NotFoundException {
        userService.deleteUser(userId);
    }
}

