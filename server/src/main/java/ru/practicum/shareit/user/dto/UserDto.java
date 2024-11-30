package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.utils.Create;

@Data
@Builder
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(groups = {Create.class}, message = "Имя не может быть пустым")
    private String name;
    @NotBlank(groups = {Create.class}, message = "Электронная почта не может быть пустой")
    @Email(groups = {Create.class}, message = "Электронная должна содержать символ @")
    private String email;
}

