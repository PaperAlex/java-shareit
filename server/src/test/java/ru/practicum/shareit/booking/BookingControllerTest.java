package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.enums.Statuses;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final BookingDto bookingDto = new BookingDto(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            1L);
    private final BookingDto bookingNullStart = new BookingDto(
            null,
            LocalDateTime.now().plusDays(2),
            1L);
    private UserDto userDto;
    private final BookingDtoOut bookingDtoOut = new BookingDtoOut(
            1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            new ItemDtoOut(1L, "ru/practicum/shareit/item", "description", true, userDto),
            userDto = new UserDto(1L, "ru/practicum/shareit/user", "des@cription.com"),
            null);

    @Test
    void createBookingTest() throws Exception {
        when(bookingService.create(any(), anyLong())).thenReturn(bookingDtoOut);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDtoOut)));
    }

    @Test
    void approveBookingTest() throws Exception {
        when(bookingService.approveBooking(anyLong(), any(), anyLong())).thenReturn(bookingDtoOut);
        bookingDtoOut.setStatus(Statuses.APPROVED);

        mvc.perform(patch("/bookings/1?approved=true")
                        .content(mapper.writeValueAsString(bookingDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDtoOut)));
    }

    @Test
    void getBookingByIdTest() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(bookingDtoOut);

        mvc.perform(get("/bookings/1")
                        .content(mapper.writeValueAsString(bookingDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDtoOut)));
    }

    @Test
    void getAllByBookerIdTest() throws Exception {
        when(bookingService.getAllByBookerId(anyString(), anyLong()))
                .thenReturn(List.of(bookingDtoOut));

        mvc.perform(get("/bookings?state=ALL")
                        .content(mapper.writeValueAsString(bookingDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(bookingDtoOut))));
    }

    @Test
    void getAllByOwnerIdTest() throws Exception {
        when(bookingService.getAllByOwnerId(anyLong(), anyString()))
                .thenReturn(List.of(bookingDtoOut));

        mvc.perform(get("/bookings/owner?state=ALL")
                        .content(mapper.writeValueAsString(bookingDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(bookingDtoOut))));
    }
}