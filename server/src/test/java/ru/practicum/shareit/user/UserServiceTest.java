package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    private final long id = 1L;
    private final UserDto userDto = new UserDto(id, "user", "user@user.com");
    private final User user = new User(id, "user", "user@user.com");


    @Test
    void testFindAllNoUsers() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        Collection<UserDto> result = userService.getUsers();

        assertTrue(result.isEmpty());
    }


    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        Collection<UserDto> targetUsers = userService.getUsers();

        Assertions.assertNotNull(targetUsers);
        assertEquals(1, targetUsers.size());
        verify(userRepository, times(1))
                .findAll();
    }

    @Test
    void testGetUserById() throws NotFoundException {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserDto actualUser = userService.getUserById(id);

        assertEquals(UserMapper.toUserDto(user), actualUser);
    }

    @Test
    void testUserNotFound() {
        when((userRepository).findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.getUserById(2L));
    }

    @Test
    void testSaveNewUser() throws DuplicatedDataException {
        when(userRepository.save(any())).thenReturn(user);

        UserDto actualUser = userService.create(userDto);

        assertEquals(userDto, actualUser);
    }

    @Test
    void testSaveNewUserWithDuplicatedEmail() {
        doThrow(DataIntegrityViolationException.class).when(userRepository).save(any(User.class));

        assertThrows(DataIntegrityViolationException.class, () -> userService.create(userDto));
    }

    @Test
    void testUpdateUser() throws ValidationException, NotFoundException, DuplicatedDataException {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserDto actualUser = userService.update(userDto, id);

        assertEquals(UserMapper.toUserDto(user), actualUser);
        verify(userRepository, times(1))
                .findById(user.getId());
    }

    @Test
    void testDeleteUser() throws ValidationException, NotFoundException {
        userService.deleteUser(1L);
        verify(userRepository, times(1))
                .deleteById(1L);
    }

    @Test
    void testUserNameIsBlank() throws ValidationException, NotFoundException, DuplicatedDataException {
        UserDto userDtoNoName = new UserDto(id, "   ", "newemail@example.com");
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserDto actualUser = userService.update(userDtoNoName, id);

        assertEquals(user.getName(), actualUser.getName()); // Имя не должно измениться
        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void testEmailIsBlank() throws ValidationException, NotFoundException, DuplicatedDataException {
        UserDto userDtoWithBlankEmail = new UserDto(id, "New Name", "   ");
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserDto actualUser = userService.update(userDtoWithBlankEmail, id);

        assertEquals(user.getEmail(), actualUser.getEmail()); // Email не должен измениться
        verify(userRepository, times(1)).findById(id);
    }
}