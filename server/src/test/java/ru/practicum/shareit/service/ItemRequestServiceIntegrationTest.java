package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestService requestService;
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User requester;
    private User otherUser;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        requestRepository.deleteAll();
        userRepository.deleteAll();

        requester = userRepository.save(User.builder().name("Requester").email("req@mail.com").build());
        otherUser = userRepository.save(User.builder().name("Other").email("other@mail.com").build());
    }

    @Test
    void create_shouldSaveRequest() {
        ItemRequestDto created = requestService.create(requester.getId(), request("Need a bike"));

        assertNotNull(created.getId());
        assertEquals("Need a bike", created.getDescription());
    }

    @Test
    void getOwn_shouldReturnUserRequests() {
        ItemRequest req = requestRepository.save(ItemRequest.builder()
                .description("Need ladder")
                .requestor(requester)
                .created(LocalDateTime.now())
                .build());

        itemRepository.save(Item.builder()
                .name("Ladder")
                .description("Tall")
                .available(true)
                .owner(otherUser)
                .requestId(req.getId())
                .build());

        List<ItemRequestDto> own = requestService.getOwn(requester.getId());

        assertEquals(1, own.size());
        assertEquals(1, own.getFirst().getItems().size());
    }

    @Test
    void getOthers_shouldReturnForeignRequests() {
        requestRepository.save(ItemRequest.builder()
                .description("Need tent")
                .requestor(otherUser)
                .created(LocalDateTime.now())
                .build());

        List<ItemRequestDto> others = requestService.getOthers(requester.getId(), 0, 10);

        assertEquals(1, others.size());
    }

    @Test
    void getById_shouldReturnRequestWithItems() {
        ItemRequest req = requestRepository.save(ItemRequest.builder()
                .description("Need projector")
                .requestor(requester)
                .created(LocalDateTime.now())
                .build());

        itemRepository.save(Item.builder()
                .name("Projector")
                .description("HD")
                .available(true)
                .owner(otherUser)
                .requestId(req.getId())
                .build());

        ItemRequestDto dto = requestService.getById(otherUser.getId(), req.getId());

        assertEquals(req.getId(), dto.getId());
        assertEquals(1, dto.getItems().size());
    }

    private ItemRequestDto request(String description) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription(description);
        return dto;
    }
}
