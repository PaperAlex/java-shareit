package ru.practicum.shareit.request.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;

import java.util.List;

public interface ItemRequestService {
    @Transactional
    ItemRequestDtoOut create(ItemRequestDto requestDtoIn, Long userId) throws NotFoundException;

    List<ItemRequestDtoOut> getRequestsByRequestor(Long userId) throws NotFoundException;

    List<ItemRequestDtoOut> getAllRequests(Integer from, Integer size, Long userId) throws NotFoundException;

    ItemRequestDtoOut getRequestById(Long requestId, Long userId) throws NotFoundException;
}
