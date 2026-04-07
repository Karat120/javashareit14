package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(User.builder().name("Owner").email("owner-b@mail.com").build());
        booker = userRepository.save(User.builder().name("Booker").email("booker-b@mail.com").build());
        item = itemRepository.save(Item.builder()
                .name("Camera")
                .description("DSLR")
                .available(true)
                .owner(owner)
                .build());
    }

    @Test
    void create_shouldSaveBooking() {
        BookingDto created = bookingService.create(booker.getId(), request(item.getId(), 1, 2));

        assertNotNull(created.getId());
        assertEquals(BookingStatus.WAITING, created.getStatus());
    }

    @Test
    void approve_shouldChangeStatus() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void getById_shouldReturnBooking() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());

        BookingDto dto = bookingService.getById(booker.getId(), booking.getId());

        assertEquals(booking.getId(), dto.getId());
    }

    @Test
    void getAllByBooker_shouldReturnBookings() {
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());

        List<BookingDto> list = bookingService.getAllByBooker(booker.getId(), "ALL");

        assertEquals(1, list.size());
    }

    @Test
    void getAllByOwner_shouldReturnBookings() {
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());

        List<BookingDto> list = bookingService.getAllByOwner(owner.getId(), "ALL");

        assertEquals(1, list.size());
    }

    private BookingDto request(Long itemId, int startDays, int endDays) {
        BookingDto dto = new BookingDto();
        dto.setItemId(itemId);
        dto.setStart(LocalDateTime.now().plusDays(startDays));
        dto.setEnd(LocalDateTime.now().plusDays(endDays));
        return dto;
    }
}
