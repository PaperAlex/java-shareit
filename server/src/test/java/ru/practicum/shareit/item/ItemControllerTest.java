package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.dto.CommentDtoOut;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final ItemDtoOut itemDtoOut = new ItemDtoOut(1L, "item", "description", true,
            new UserDto(1L, "User", "qwer@sf.com"));

    @Test
    void createItemTest() throws Exception {
        when(itemService.create(anyLong(), any())).thenReturn(itemDtoOut);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemDtoOut)))
                .andExpect(jsonPath("$.id", is(itemDtoOut.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoOut.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDtoOut.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDtoOut.getAvailable()), Boolean.class));
    }

    @Test
    void updateItemTest() throws Exception {
        when(itemService.update(anyLong(), any(), anyLong())).thenReturn(itemDtoOut);

        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemDtoOut)))
                .andExpect(jsonPath("$.id", is(itemDtoOut.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoOut.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDtoOut.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDtoOut.getAvailable()), Boolean.class));
    }

    @Test
    void getItemByIdTest() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemDtoOut);

        mvc.perform(get("/items/1")
                        .content(mapper.writeValueAsString(itemDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoOut.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoOut.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoOut.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoOut.getAvailable())));
    }

    @Test
    void getItemsByUserTest() throws Exception {
        when(itemService.getItemByUser(anyLong())).thenReturn(List.of(itemDtoOut));

        mvc.perform(get("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemDtoOut))));
    }

    @Test
    void searchItemByTextTest() throws Exception {
        when(itemService.searchItemByText(any())).thenReturn(List.of(itemDtoOut));

        mvc.perform(get("/items/search?text=a")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemDtoOut))));
    }

    @Test
    void addCommentTest() throws Exception {
        final CommentDtoOut commentDtoOut = new CommentDtoOut(1L, "comment", "ru/practicum/shareit/user",
                LocalDateTime.now());

        when(itemService.addComment(anyLong(), any(), anyLong())).thenReturn(commentDtoOut);

        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(commentDtoOut)))
                .andExpect(jsonPath("$.id", is(commentDtoOut.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDtoOut.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDtoOut.getAuthorName())));
    }
}