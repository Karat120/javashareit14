package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto requestDto) {
        User requestor = getUser(userId);
        ItemRequest request = ItemRequest.builder()
                .description(requestDto.getDescription())
                .created(LocalDateTime.now())
                .requestor(requestor)
                .build();
        return ItemRequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        getUser(userId);
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        return toDtosWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getOthers(Long userId, Integer from, Integer size) {
        getUser(userId);
        int page = from / size;
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId,
                PageRequest.of(page, size));
        return toDtosWithItems(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUser(userId);
        ItemRequest request = getRequest(requestId);
        return toDtosWithItems(List.of(request)).getFirst();
    }

    private List<ItemRequestDto> toDtosWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<Item>> itemsByRequest = itemRepository.findAllByRequestIdIn(requestIds).stream()
                .filter(item -> item.getRequestId() != null)
                .collect(Collectors.groupingBy(Item::getRequestId));

        List<ItemRequestDto> result = new ArrayList<>();
        for (ItemRequest request : requests) {
            ItemRequestDto dto = ItemRequestMapper.toDto(request);
            dto.setItems(itemsByRequest.getOrDefault(request.getId(), List.of()).stream()
                    .map(ItemRequestMapper::toRequestItemDto)
                    .toList());
            result.add(dto);
        }
        return result;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private ItemRequest getRequest(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
    }
}
