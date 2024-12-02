package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/requests")
public class ItemRequestController {
    private final ItemRequestServiceImpl requestService;

    @PostMapping
    public ItemRequestDtoOut create(@RequestBody ItemRequestDto requestDtoIn,
                                    @RequestHeader("X-Sharer-User-Id") long userId) throws NotFoundException {
        return requestService.create(requestDtoIn, userId);
    }

    @GetMapping
    public List<ItemRequestDtoOut> getRequestsByRequestor(@RequestHeader("X-Sharer-User-Id") long userId) throws NotFoundException {
        return requestService.getRequestsByRequestor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoOut> getAllRequests(@RequestParam(defaultValue = "1") Integer from,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestHeader("X-Sharer-User-Id") long userId) throws NotFoundException {
        return requestService.getAllRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoOut getRequestById(@PathVariable long requestId,
                                            @RequestHeader("X-Sharer-User-Id") long userId) throws NotFoundException {
        return requestService.getRequestById(requestId, userId);
    }
}