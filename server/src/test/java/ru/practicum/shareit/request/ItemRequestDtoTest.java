package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;


import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class ItemRequestDtoTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testItemRequestDto() throws Exception {
        ItemRequestDto dto = new ItemRequestDto("description");

        String json = objectMapper.writeValueAsString(dto);
        ItemRequestDto deserializedDto = objectMapper.readValue(json, ItemRequestDto.class);

        assertEquals(dto.getDescription(), deserializedDto.getDescription());

    }
}