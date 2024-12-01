package ru.practicum.shareit.user;

import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;
import org.springframework.http.ResponseEntity;

public class UserClient extends BaseClient {
    public UserClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("");
    }

    public ResponseEntity<Object> getUserById(long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> saveNewUser(UserDto userDto) {
        return post("", userDto);
    }

    public ResponseEntity<Object> updateUser(long userId, UserDto userDto) {
        return patch("/" + userId, userId, userDto);
    }

    public ResponseEntity<Object> deleteUser(long userId) {
        return delete("/" + userId);
    }
}