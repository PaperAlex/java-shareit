package ru.practicum.shareit.request.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoName;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestDtoOut create(ItemRequestDto itemRequestDto, Long userId) throws NotFoundException {
        log.info("Создание нового запроса {}", itemRequestDto.getDescription());
        User requestor = findUserById(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto);
        request.setCreated(LocalDateTime.now());
        request.setRequestor(requestor);
        return ItemRequestMapper.toItemRequestDtoOut(requestRepository.save(request));
    }

    @Override
    public List<ItemRequestDtoOut> getRequestsByRequestor(Long userId) throws NotFoundException {
        log.info("Обработка всех запросов по id пользователя {}", userId);
        findUserById(userId);
        List<ItemRequest> requests = requestRepository.findAllByRequestorId(userId, Sort.by(DESC, "created"));
        return addItemsList(requests);
    }

    @Override
    public List<ItemRequestDtoOut> getAllRequests(Integer from, Integer size, Long userId) throws NotFoundException {
        log.info("Обработка всех запросов");
        findUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdIsNot(userId, pageable);
        return addItemsList(requests);
    }

    @Override
    public ItemRequestDtoOut getRequestById(Long requestId, Long userId) throws NotFoundException {
        log.info("Обработка запроса по id {}", requestId);
        findUserById(userId);
        ItemRequestDtoOut requestDtoOut = ItemRequestMapper.toItemRequestDtoOut(requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Запрос %d не найден", requestId))));

        List<ItemDtoName> list = new ArrayList<>();
        for (Item item : itemRepository.findAllByRequestId(requestId)) {
            ItemDtoName itemDtoName = ItemMapper.toItemDtoName(item);
            list.add(itemDtoName);
        }
        requestDtoOut.setItems(list);
        return requestDtoOut;
    }

    private List<ItemRequestDtoOut> addItemsList(List<ItemRequest> requests) {
        List<ItemRequestDtoOut> requestsOut = new ArrayList<>();
        for (ItemRequest request : requests) {
            ItemRequestDtoOut requestDtoOut = ItemRequestMapper.toItemRequestDtoOut(request);
            List<ItemDtoName> items = itemRepository.findAllByRequestId(request.getId()).stream()
                    .map(ItemMapper::toItemDtoName).collect(Collectors.toList());
            requestDtoOut.setItems(items);
            log.info(items.toString());
            requestsOut.add(requestDtoOut);
        }
        return requestsOut;
    }

    public User findUserById(Long userId) throws NotFoundException {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь %d не найден", userId)));
    }
}