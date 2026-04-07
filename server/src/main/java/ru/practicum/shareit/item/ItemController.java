package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String HEADER_USER = "X-Sharer-User-Id";

    @GetMapping("/{itemId}")
    public ItemDto findItemById(@RequestHeader(HEADER_USER) Long userId,
                                @PathVariable Long itemId) {
        log.info("Request item ID: {} from user: {}", itemId, userId);
        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> findItemsByOwner(@RequestHeader(HEADER_USER) Long userId) {
        log.info("Request all items for owner: {}", userId);
        return itemService.getByOwner(userId);
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader(HEADER_USER) Long userId,
                           @Valid @RequestBody ItemDto dto) {
        log.info("Adding new item for user: {}", userId);
        return itemService.create(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto editItem(@RequestHeader(HEADER_USER) Long userId,
                            @PathVariable Long itemId,
                            @RequestBody ItemDto dto) {
        log.info("Updating item ID: {} by user: {}", itemId, userId);
        return itemService.update(userId, itemId, dto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByText(@RequestParam String text) {
        log.info("Searching items with query: {}", text);
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(HEADER_USER) Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto dto) {
        log.info("User {} adding comment to item {}", userId, itemId);
        return itemService.createComment(userId, itemId, dto);
    }
}