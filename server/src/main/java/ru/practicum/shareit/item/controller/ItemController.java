package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController("ServerItemController")
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;


    @PostMapping
    public ItemDtoOut create(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @RequestBody ItemDto itemDto) throws NotFoundException {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoOut update(@PathVariable Long itemId, @RequestBody ItemDto itemDto,
                             @RequestHeader("X-Sharer-User-Id") Long ownerId) throws ValidationException, NotFoundException {
        return itemService.update(itemId, itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDtoOut getItem(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) throws NotFoundException {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public Collection<ItemDtoOut> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) throws NotFoundException {
        return itemService.getItemByUser(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDtoOut> geItemBySearch(@RequestParam String text) {
        return itemService.searchItemByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoOut addComment(@PathVariable Long itemId,
                                    @RequestBody CommentDto commentDto,
                                    @RequestHeader("X-Sharer-User-Id") long userId) throws ValidationException, NotFoundException {
        return itemService.addComment(itemId, commentDto, userId);
    }
}

