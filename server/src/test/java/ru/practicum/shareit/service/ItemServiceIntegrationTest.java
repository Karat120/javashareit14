package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemServiceIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(User.builder().name("Owner").email("owner@mail.com").build());
        booker = userRepository.save(User.builder().name("Booker").email("booker@mail.com").build());
        item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(owner)
                .build());
    }

    @Test
    void create_shouldSaveItem() {
        ItemDto dto = new ItemDto();
        dto.setName("Saw");
        dto.setDescription("Wood saw");
        dto.setAvailable(true);

        ItemDto created = itemService.create(owner.getId(), dto);

        assertNotNull(created.getId());
        assertEquals("Saw", created.getName());
    }

    @Test
    void update_shouldPatchItem() {
        ItemDto patch = new ItemDto();
        patch.setDescription("Updated description");

        ItemDto updated = itemService.update(owner.getId(), item.getId(), patch);

        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    void getById_shouldReturnItemWithComments() {
        createPastApprovedBooking();
        itemService.createComment(booker.getId(), item.getId(), comment("Great item"));

        ItemDto dto = itemService.getById(item.getId(), owner.getId());

        assertEquals(item.getId(), dto.getId());
        assertEquals(1, dto.getComments().size());
    }

    @Test
    void getByOwner_shouldReturnOwnersItems() {
        List<ItemDto> items = itemService.getByOwner(owner.getId());

        assertEquals(1, items.size());
        assertEquals(item.getId(), items.getFirst().getId());
    }

    @Test
    void search_shouldFindByText() {
        List<ItemDto> found = itemService.search("drill");

        assertEquals(1, found.size());
        assertEquals(item.getId(), found.getFirst().getId());
    }

    @Test
    void createComment_shouldPersistCommentForBooker() {
        createPastApprovedBooking();

        CommentDto saved = itemService.createComment(booker.getId(), item.getId(), comment("Useful"));

        assertNotNull(saved.getId());
        assertEquals("Useful", saved.getText());
    }

    private void createPastApprovedBooking() {
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build());
    }

    private CommentDto comment(String text) {
        CommentDto dto = new CommentDto();
        dto.setText(text);
        return dto;
    }
}
