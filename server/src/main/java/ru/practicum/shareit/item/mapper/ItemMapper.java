package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoName;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;


@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    /* Метод преобразовывает данные в ItemDto */
    public static ItemDtoOut toItemDto(Item item) {
        ItemDtoOut itemDtoOut = new ItemDtoOut(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                UserMapper.toUserDto(item.getOwner())
        );
        if (item.getRequest() != null) {
            itemDtoOut.setRequestId(item.getRequest().getId());
        }
        return itemDtoOut;
    }

    /* Метод преобразовывает данные в Item */
    public static Item toItem(ItemDto itemDto) {
        return new Item(
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable()
        );
    }

    public static ItemDtoName toItemDtoName(Item item) {
        return new ItemDtoName(
                item.getId(),
                item.getName(),
                item.getOwner().getId()
        );
    }
}

