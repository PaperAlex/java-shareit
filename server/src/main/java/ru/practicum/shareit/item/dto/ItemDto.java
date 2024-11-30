package ru.practicum.shareit.item.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.utils.Create;


@Data
@Builder
@AllArgsConstructor
public class ItemDto {
    @NotBlank(groups = {Create.class}, message = "Название не может быть пустым")
    private String name;
    @NotBlank(groups = {Create.class}, message = "Описание не может быть пустым")
    private String description;
    @NotNull(groups = {Create.class}, message = "Статус не может быть пустым")
    private Boolean available;
    private Long requestId;
}
