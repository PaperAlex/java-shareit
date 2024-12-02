package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentDtoOut;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class CommentDtoTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testJsonCommentDto() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("text");

        String json = objectMapper.writeValueAsString(commentDto);
        CommentDto deserializedDto = objectMapper.readValue(json, CommentDto.class);

        assertEquals(commentDto.getText(), deserializedDto.getText());
    }

    @Test
    public void testJsonCommentDtoOut() throws Exception {
        CommentDtoOut commentDtoOut = new CommentDtoOut(1L, "text", "name",
                LocalDateTime.of(2026, 11, 19, 11, 11, 11));

        String json = objectMapper.writeValueAsString(commentDtoOut);
        CommentDto deserializedDto = objectMapper.readValue(json, CommentDto.class);

        assertEquals(commentDtoOut.getText(), deserializedDto.getText());
    }

}