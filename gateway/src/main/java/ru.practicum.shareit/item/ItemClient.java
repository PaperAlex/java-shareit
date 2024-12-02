package ru.practicum.shareit.item;

import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import org.springframework.http.ResponseEntity;

import java.util.Map;

//@Service
public class ItemClient extends BaseClient {

    public ItemClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> saveNewItem(ItemDto itemDto, long userId) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(long itemId, ItemDto itemDto, long userId) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getItemById(long itemId, long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getItemsByOwner(Integer from, Integer size, long userId) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getItemBySearch(Integer from, Integer size, String text, long userId) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> saveNewComment(long itemId, CommentDto commentDto, long userId) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }
}