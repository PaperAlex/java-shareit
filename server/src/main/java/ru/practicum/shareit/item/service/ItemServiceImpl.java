package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.enums.Statuses;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;


    @Transactional
    @Override
    public ItemDtoOut create(Long ownerId, ItemDto itemDto) throws NotFoundException {
        User owner = userRepository.findById(ownerId).orElseThrow(() ->
                new NotFoundException("Пользователь с таким ID не найден!"));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Long requestId = itemDto.getRequestId();
        if (requestId != null) {
            item.setRequest(itemRequestRepository.findById(requestId).orElseThrow(() ->
                    new NotFoundException(String.format("Запрос с id %d не найден", requestId))));
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDtoOut getItemById(Long itemId, Long userId) throws NotFoundException {
        return itemRepository.findById(itemId).map(item -> commentsAndBooking(item, userId)).orElseThrow(() ->
                new NotFoundException(String.format("id %s не найден", itemId)));
    }

    @Transactional
    @Override
    public ItemDtoOut update(Long itemId, ItemDto newItemDto, Long ownerId) throws ValidationException, NotFoundException {
        User owner = userRepository.findById(ownerId).orElseThrow(() ->
                new NotFoundException("Пользователь с таким ID не найден!"));
        Item newItem = ItemMapper.toItem(newItemDto);
        Item item = itemRepository.findById(itemId).get();
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ValidationException("Запрос от пользователя, не являющимся владельцем товара");
        }
        if (newItem.getName() != null) {
            item.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            item.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            item.setAvailable(newItem.getAvailable());
        }
        return ItemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public void deleteItem(Long itemId) throws ValidationException, NotFoundException {

    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDtoOut> getItemByUser(Long userId) throws NotFoundException {
        User owner = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с таким ID не найден!"));
        Collection<Item> items = itemRepository.findAllByOwnerId(userId);
        if (!items.isEmpty()) {
            return itemsDtoList(items);
        }
        return Collections.emptyList();
    }

    private Collection<ItemDtoOut> itemsDtoList(Collection<Item> items) {
        List<Long> itemsIds = items.stream().map(Item::getId).toList();

        Collection<ItemDtoOut> itemsList = new ArrayList<>();
        for (Item item : items) itemsList.add(ItemMapper.toItemDto(item));
        return itemsList;
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDtoOut> searchItemByText(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        return itemRepository.findItems(text).stream().map(ItemMapper::toItemDto).collect(toList());
    }

    @Transactional
    @Override
    public CommentDtoOut addComment(Long itemId, CommentDto commentDto, Long userId) throws ValidationException, NotFoundException {
        User user = getUser(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Пользователь с таким ID не найден!"));
        if (!bookingRepository.existsByBookerIdAndItemIdAndEndBefore(user.getId(), item.getId(), LocalDateTime.now())) {
            throw new ValidationException("Пользователь не пользовался вещью");
        }
        Comment comment = commentRepository.save(CommentMapper.toComment(commentDto, item, user));
        return CommentMapper.toCommentDtoOut(comment);
    }

    private ItemDtoOut commentsAndBooking(Item item, Long userId) {
        ItemDtoOut itemDtoOut = ItemMapper.toItemDto(item);

        LocalDateTime timeNow = LocalDateTime.now();
        if (itemDtoOut.getOwner().getId().equals(userId)) {
            itemDtoOut.setLastBooking(bookingRepository
                    .findFirstByItemIdAndStartLessThanEqualAndStatus(itemDtoOut.getId(), timeNow,
                            Statuses.APPROVED, Sort.by(DESC, "end"))
                    .map(BookingMapper::toBookingDtoOut)
                    .orElse(null));

            itemDtoOut.setNextBooking(bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatus(itemDtoOut.getId(), timeNow,
                            Statuses.APPROVED, Sort.by(ASC, "end"))
                    .map(BookingMapper::toBookingDtoOut)
                    .orElse(null));
        }

        itemDtoOut.setComments(commentRepository.findAllByItemId(itemDtoOut.getId())
                .stream()
                .map(CommentMapper::toCommentDtoOut)
                .collect(toList()));

        return itemDtoOut;
    }

    private User getUser(Long userId) throws NotFoundException {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь %d не найден", userId)));
    }
}

