package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemDto dto);

    ItemDto update(Long userId, Long itemId, ItemDto dto);

    ItemDto getById(Long itemId, Long userId);

    List<ItemDto> getByOwner(Long userId);

    List<ItemDto> search(String query);

    CommentDto createComment(Long userId, Long itemId, CommentDto dto);
}