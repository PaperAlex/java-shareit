package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.utils.Create;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class BookingDto {

    @NotNull(groups = {Create.class})
    private LocalDateTime start;

    @NotNull(groups = {Create.class})
    private LocalDateTime end;

    @NotNull(groups = {Create.class})
    private Long itemId;
}
