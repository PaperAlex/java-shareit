package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Transactional
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;


    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) throws NotFoundException {
        User owner = userRepository.findById(ownerId).orElseThrow(() ->
                new NotFoundException("Пользователь с таким ID не найден!"));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDtoOut getItemById(Long itemId, Long userId) throws NotFoundException {
        return itemRepository.findById(itemId).map(item -> commentsAndBooking(item, userId)).orElseThrow(() ->
                new NotFoundException(String.format("id %s не найден", itemId)));
    }

    @Override
    public ItemDto update(Long itemId, ItemDto newItemDto, Long ownerId) throws ValidationException, NotFoundException {
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

    @Override
    public void deleteItem(Long itemId) throws ValidationException, NotFoundException {

    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> getItemByUser(Long userId) throws NotFoundException {
        User owner = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с таким ID не найден!"));
        Collection<Item> items = itemRepository.findAllByOwnerId(userId);
        if (!items.isEmpty()) {
            return itemsDtoList(items);
        }
        return Collections.emptyList();
    }

    private Collection<ItemDto> itemsDtoList(Collection<Item> items) {
        List<Long> itemsIds = items.stream().map(Item::getId).toList();

        Collection<ItemDto> itemsList = new ArrayList<>();
        for (Item item : items) {
            itemsList.add(ItemMapper.toItemDto(item));
        }
        return itemsList;
    }


    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> searchItemByText(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        return itemRepository.findItems(text).stream().map(ItemMapper::toItemDto).collect(toList());
    }

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
        ItemDtoOut itemDtoOut = ItemMapper.toDto(item);

        LocalDateTime timeNow = LocalDateTime.now();
        if (itemDtoOut.getOwner().getId() == userId) {
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

    private List<ItemDtoOut> commentsAndBookingList(List<Item> items) {
        LocalDateTime thisMoment = LocalDateTime.now();

        Map<Item, Booking> itemsWithLastBookings = bookingRepository
                .findByItemInAndStartLessThanEqualAndStatus(items, thisMoment,
                        Statuses.APPROVED, Sort.by(DESC, "end"))
                .stream()
                .collect(Collectors.toMap(Booking::getItem, Function.identity(), (o1, o2) -> o1));

        Map<Item, Booking> itemsWithNextBookings = bookingRepository
                .findByItemInAndStartAfterAndStatus(items, thisMoment,
                        Statuses.APPROVED, Sort.by(ASC, "end"))
                .stream()
                .collect(Collectors.toMap(Booking::getItem, Function.identity(), (o1, o2) -> o1));

        Map<Item, List<Comment>> itemsWithComments = commentRepository
                .findByItemIn(items, Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));

        List<ItemDtoOut> itemDtoOuts = new ArrayList<>();
        for (Item item : items) {
            ItemDtoOut itemDtoOut = ItemMapper.toDto(item);
            Booking lastBooking = itemsWithLastBookings.get(item);
            if (itemsWithLastBookings.size() > 0 && lastBooking != null) {
                itemDtoOut.setLastBooking(BookingMapper.toBookingDtoOut(lastBooking));
            }
            Booking nextBooking = itemsWithNextBookings.get(item);
            if (itemsWithNextBookings.size() > 0 && nextBooking != null) {
                itemDtoOut.setNextBooking(BookingMapper.toBookingDtoOut(nextBooking));
            }
            List<CommentDtoOut> commentDtoOuts = itemsWithComments.getOrDefault(item, Collections.emptyList())
                    .stream()
                    .map(CommentMapper::toCommentDtoOut)
                    .collect(toList());
            itemDtoOut.setComments(commentDtoOuts);

            itemDtoOuts.add(itemDtoOut);
        }
        return itemDtoOuts;
    }

    private User getUser(Long userId) throws NotFoundException {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь %d не найден", userId)));
    }
}

