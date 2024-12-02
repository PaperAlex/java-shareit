package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class ItemDtoTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testJsonItemDto() throws Exception {
        ItemDto itemDto = new ItemDto("name", "description", true, 1L);

        String json = objectMapper.writeValueAsString(itemDto);
        ItemDto deserializedDto = objectMapper.readValue(json, ItemDto.class);

        assertEquals(itemDto.getName(), deserializedDto.getName());
        assertEquals(itemDto.getDescription(), deserializedDto.getDescription());
        assertEquals(itemDto.getAvailable(), deserializedDto.getAvailable());
        assertEquals(itemDto.getRequestId(), deserializedDto.getRequestId());
    }
}