package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class UserDtoTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testJsonUserDto() throws Exception {
        UserDto userDto = new UserDto(1L, "name", "name@mail.com");

        String json = objectMapper.writeValueAsString(userDto);
        UserDto deserializedDto = objectMapper.readValue(json, UserDto.class);

        assertEquals(userDto.getId(), deserializedDto.getId());
        assertEquals(userDto.getName(), deserializedDto.getName());
        assertEquals(userDto.getEmail(), deserializedDto.getEmail());
    }
}