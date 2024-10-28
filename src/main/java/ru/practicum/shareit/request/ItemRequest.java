package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */

@Data
@Builder
public class ItemRequest {
    private Long id;
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    /* requestor — пользователь, создавший запрос (userId) */
    private Long requestorId;
    private LocalDateTime created;
}
