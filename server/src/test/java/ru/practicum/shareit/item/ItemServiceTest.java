package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    private final long id = 1L;
    private final long notOwnerId = 2L;
    private final User user = new User(id, "User", "user@user.ru");
    private final User notOwner = new User(2L, "User2", "user2@user2.ru");
    private final ItemDto itemDto = new ItemDto("item", "description", true, null);
    private final ItemDtoOut itemDtoOut = new ItemDtoOut(id, "item", "description", true,
            new UserDto(id, "User", "user@user.ru"));
    private final Item item = new Item(id, "item", "description", true, user, null);
    private final Item anotherItem = new Item(id, "item2", "description", true, user, null);
    private final CommentDtoOut commentDto = new CommentDtoOut(id, "qwerty", "User",
            LocalDateTime.of(2023, 7, 1, 12, 12, 12));
    private final Comment comment = new Comment(id, "qwerty", item, user,
            LocalDateTime.of(2023, 7, 1, 12, 12, 12));
    private final Booking booking = new Booking(id, null, null, item, user, Statuses.WAITING);
    private final ItemRequest itemRequest = new ItemRequest();

    @Test
    void testSaveItem() throws NotFoundException {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDtoOut actualItemDto = itemService.create(id, itemDto);

        Assertions.assertEquals(ItemMapper.toItemDto(item), actualItemDto);
        Assertions.assertNull(item.getRequest());
    }

    @Test
    void testNotToSaveItemUserDoesNotExist() {
        when((userRepository).findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.create(2L, itemDto));
    }

    @Test
    void shouldNotToSaveItemNameIsMissing() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        doThrow(DataIntegrityViolationException.class).when(itemRepository).save(any(Item.class));

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> itemService.create(id, itemDto));
    }

    @Test
    void testUpdateItem() throws ValidationException, NotFoundException {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        ItemDtoOut actualItemDto = itemService.update(id, itemDto, id);

        Assertions.assertEquals(itemDtoOut, actualItemDto);
    }

    @Test
    void testNotUpdateItem() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(notOwner));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () -> itemService.update(id, itemDto, 2L));
    }

    @Test
    void testReturnItem() throws NotFoundException {
        when(bookingRepository.findFirstByItemIdAndStartLessThanEqualAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(booking));
        when(commentRepository.findAllByItemId(id)).thenReturn(List.of(comment));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        final ItemDtoOut itemDto = ItemMapper.toItemDto(item);
        itemDto.setLastBooking(BookingMapper.toBookingDtoOut(booking));
        itemDto.setNextBooking(BookingMapper.toBookingDtoOut(booking));
        itemDto.setComments(List.of(CommentMapper.toCommentDtoOut(comment)));

        ItemDtoOut actualItemDto = itemService.getItemById(id, id);

        Assertions.assertEquals(itemDto, actualItemDto);
    }

    @Test
    void testItemDoesNotExist() {
        when((itemRepository).findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.getItemById(2L, id));
    }

    @Test
    void testSearchItems() {
        when(itemRepository.findItems(any())).thenReturn(List.of(item));

        Collection<ItemDtoOut> targetItems = itemService.searchItemByText("qwerty");

        Assertions.assertNotNull(targetItems);
        Assertions.assertEquals(1, targetItems.size());
        verify(itemRepository, times(1))
                .findItems(any());
    }

    @Test
    void testSearchTextIsBlank() {
        Collection<ItemDtoOut> targetItems = itemService.searchItemByText("");

        Assertions.assertTrue(targetItems.isEmpty());
        Assertions.assertEquals(0, targetItems.size());
        verify(itemRepository, never()).findItems(any());
    }

    @Test
    void testAddComment() throws ValidationException, NotFoundException {
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(commentRepository.save(any())).thenReturn(comment);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        CommentDtoOut actualComment = itemService.addComment(id, new CommentDto("qwerty"), id);

        Assertions.assertEquals(commentDto, actualComment);
    }

    @Test
    void testUserIsNotBooker() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any())).thenReturn(false);

        Assertions.assertThrows(ValidationException.class, () ->
                itemService.addComment(id, new CommentDto("qwerty"), id));
    }

    @Test
    void testRequestDoesNotExist() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(requestRepository.findById(itemRequest.getId())).thenReturn(Optional.empty());

        itemDto.setRequestId(itemRequest.getId());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.create(id, itemDto));
    }

    @Test
    void testUpdateItemAvailability() throws ValidationException, NotFoundException {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        itemDto.setAvailable(false);
        ItemDtoOut actualItemDto = itemService.update(id, itemDto, id);

        Assertions.assertFalse(actualItemDto.getAvailable());
    }

    @Test
    void testGettingNonexistentItem() {
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.getItemById(id, id));
    }

    @Test
    void testOwnerHasNoItems() throws NotFoundException {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(Collections.emptyList());

        Collection<ItemDtoOut> targetItems = itemService.getItemByUser(id);

        Assertions.assertTrue(targetItems.isEmpty());
    }

    @Test
    void testSearchTextMatchesMultipleItems() {
        when(itemRepository.findItems(any())).thenReturn(List.of(item, anotherItem));

        Collection<ItemDtoOut> targetItems = itemService.searchItemByText("qwerty");

        Assertions.assertEquals(2, targetItems.size());
    }

    @Test
    void testCommentTextIsEmpty() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                itemService.addComment(id, new CommentDto(""), id));
    }

    @Test
    void testUserExistsAndRequestExists() throws NotFoundException {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(requestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));

        Item item = new Item();
        item.setId(1L);
        item.setRequest(itemRequest);
        item.setOwner(user);

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        itemDto.setRequestId(itemRequest.getId());
        ItemDtoOut actualItemDto = itemService.create(id, itemDto);

        Assertions.assertEquals(ItemMapper.toItemDto(item), actualItemDto);
        Assertions.assertNotNull(item.getRequest());
    }

    @Test
    void testUserIsNotOwner() throws NotFoundException {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        ItemDtoOut actualItemDto = itemService.getItemById(id, notOwnerId);
        ItemDtoOut expectedItemDto = ItemMapper.toItemDto(item);
        expectedItemDto.setComments(Collections.emptyList());

        Assertions.assertEquals(expectedItemDto, actualItemDto);
    }

    @Test
    void testItemWithNoComments() throws NotFoundException {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(id)).thenReturn(Collections.emptyList());

        ItemDtoOut actualItemDto = itemService.getItemById(id, notOwnerId);

        Assertions.assertTrue(actualItemDto.getComments().isEmpty());
    }

    @Test
    void testChangingAvailability() throws ValidationException, NotFoundException {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        itemDto.setAvailable(true);
        ItemDtoOut actualItemDto = itemService.update(id, itemDto, id);

        Assertions.assertTrue(actualItemDto.getAvailable());
    }

    @Test
    void testItemsFilteredByOwner() throws NotFoundException {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of(item, anotherItem));

        Collection<ItemDtoOut> targetItems = itemService.getItemByUser(id);

        Assertions.assertEquals(2, targetItems.size());
    }


    @Test
    void testOwnerHasNoItemsWithPaging() throws NotFoundException {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(Collections.emptyList());

        Collection<ItemDtoOut> targetItems = itemService.getItemByUser(id);

        Assertions.assertTrue(targetItems.isEmpty());
    }

    @Test
    void testSearchTextMatchesMultipleItemsWithDifferentCases() {
        when(itemRepository.findItems(any())).thenReturn(List.of(item, anotherItem));

        Collection<ItemDtoOut> targetItems = itemService.searchItemByText("ITEM");

        Assertions.assertEquals(2, targetItems.size());
    }

    @Test
    void testSavingCommentForNonExistentItem() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.addComment(id, new CommentDto("qwerty"), id));
    }

    @Test
    void testUserTriesToSaveCommentWithoutBooking() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any())).thenReturn(false);

        Assertions.assertThrows(ValidationException.class, () -> itemService.addComment(id, new CommentDto("qwerty"), id));
    }

    @Test
    void testUserIsTheOwnerAndFieldsAreNull() throws ValidationException, NotFoundException {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        itemDto.setDescription("New description");
        itemDto.setAvailable(null);

        ItemDtoOut actualItemDto = itemService.update(id, itemDto, id);

        Assertions.assertEquals(item.getName(), actualItemDto.getName());
        Assertions.assertEquals(item.getDescription(), actualItemDto.getDescription());
        Assertions.assertEquals(item.getAvailable(), actualItemDto.getAvailable());
    }

    @Test
    void testItemIsAvailable() throws NotFoundException {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndStartLessThanEqualAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(booking));
        when(commentRepository.findAllByItemId(id)).thenReturn(List.of(comment));

        ItemDtoOut actualItemDto = itemService.getItemById(id, id);

        Assertions.assertTrue(actualItemDto.getAvailable());
    }

    @Test
    void testItemIsNotAvailable() throws NotFoundException {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndStartLessThanEqualAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(booking));
        when(commentRepository.findAllByItemId(id)).thenReturn(List.of(comment));

        item.setAvailable(false);

        ItemDtoOut actualItemDto = itemService.getItemById(id, id);

        Assertions.assertFalse(actualItemDto.getAvailable());
    }

    @Test
    void testUpdatingItemWithNullDescription() {
        itemDto.setDescription(null);

        Assertions.assertThrows(NotFoundException.class, () -> itemService.update(id, itemDto, id));
    }

    @Test
    void testSearchTextMatchesMultipleItemsWithDifferentCasesInsensitive() {
        when(itemRepository.findItems(any())).thenReturn(List.of(item, anotherItem));

        Collection<ItemDtoOut> targetItems = itemService.searchItemByText("It");

        Assertions.assertEquals(2, targetItems.size());
    }

    @Test
    void testSavingCommentForItemWithoutBooking() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any())).thenReturn(false);

        Assertions.assertThrows(ValidationException.class, () -> itemService.addComment(id, new CommentDto("qwerty"), id));
    }

    @Test
    void testUserTriesToSaveCommentForNonExistentItem() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.addComment(id, new CommentDto("qwerty"), id));
    }
}