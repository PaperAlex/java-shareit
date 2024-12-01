package ru.practicum.shareit.request;

import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.RequestDto;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class RequestClient extends BaseClient {
    public RequestClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> saveRequest(RequestDto requestDto, long userId) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getRequestsByRequestor(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAllRequests(Integer from, Integer size, long userId) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/all?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getRequestById(long requestId, long userId) {
        return get("/" + requestId, userId);
    }
}