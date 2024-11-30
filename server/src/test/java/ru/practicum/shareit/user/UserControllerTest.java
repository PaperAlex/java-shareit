package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final UserDto userDto = new UserDto(1L, "user", "user@user.ru");

    @Test
    void getUsersTest() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(userDto));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())));
    }

    @Test
    void getUserByIdTest() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(userDto);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void createUserTest() throws Exception {
        when(userService.create(any())).thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail()), String.class));
    }

    @Test
    void updateUserTest() throws Exception {
        when(userService.update(any(), anyLong())).thenReturn(userDto);

        mvc.perform(patch("/users/1", userDto.getId())
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUserTest() throws Exception {
        mvc.perform(delete("/users/100"))
                .andExpect(status().isOk());
        Mockito.verify(userService, Mockito.times(1))
                .deleteUser(anyLong());
    }

    @Test
    void getUserByIdNotFoundTest() throws Exception {
        when(userService.getUserById(anyLong())).thenThrow(new NotFoundException("User  not found"));

        mvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserNotFound() throws Exception {
        when(userService.update(any(), anyLong())).thenThrow(new NotFoundException("Пользователь не найден"));

        mvc.perform(patch("/users/999")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserNotFoundTest() throws Exception {
        doThrow(new NotFoundException("Пользователь не найден")).when(userService).deleteUser(anyLong());

        mvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsersEmptyListTest() throws Exception {
        when(userService.getUsers()).thenReturn(Collections.emptyList());

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}