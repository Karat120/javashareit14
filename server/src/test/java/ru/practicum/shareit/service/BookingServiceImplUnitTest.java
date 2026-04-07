package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private BookingServiceImpl service;
    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        service = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
        owner = User.builder().id(1L).name("owner").email("o@mail.com").build();
        booker = User.builder().id(2L).name("booker").email("b@mail.com").build();
        item = Item.builder().id(10L).name("item").description("d").available(true).owner(owner).build();
    }

    @Test
    void create_shouldThrowWhenItemUnavailable() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Item unavailable = Item.builder()
                .id(10L)
                .name("item")
                .description("d")
                .available(false)
                .owner(owner)
                .build();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(unavailable));
        BookingDto dto = request(10L, 1, 2);

        assertThrows(ValidationException.class, () -> service.create(2L, dto));
    }

    @Test
    void create_shouldThrowWhenOwnerBooksOwnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> service.create(1L, request(10L, 1, 2)));
    }

    @Test
    void create_shouldThrowOnInvalidDates() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        BookingDto dto = request(10L, 2, 1);

        assertThrows(ValidationException.class, () -> service.create(2L, dto));
    }

    @Test
    void approve_shouldRejectNonOwner() {
        Booking booking = waitingBooking();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> service.approve(99L, 1L, true));
    }

    @Test
    void approve_shouldRejectNonWaiting() {
        Booking booking = waitingBooking();
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> service.approve(1L, 1L, true));
    }

    @Test
    void getById_shouldRejectForeignUser() {
        Booking booking = waitingBooking();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> service.getById(777L, 1L));
    }

    @Test
    void getAllByBooker_shouldCoverAllStatesAndDefault() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(eq(2L), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(eq(2L), any(), any(), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByBookerIdAndEndBefore(eq(2L), any(), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByBookerIdAndStartAfter(eq(2L), any(), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByBookerIdAndStatus(eq(2L), any(), any(Sort.class))).thenReturn(List.of());

        assertDoesNotThrow(() -> service.getAllByBooker(2L, "ALL"));
        assertDoesNotThrow(() -> service.getAllByBooker(2L, "CURRENT"));
        assertDoesNotThrow(() -> service.getAllByBooker(2L, "PAST"));
        assertDoesNotThrow(() -> service.getAllByBooker(2L, "FUTURE"));
        assertDoesNotThrow(() -> service.getAllByBooker(2L, "WAITING"));
        assertDoesNotThrow(() -> service.getAllByBooker(2L, "REJECTED"));
        assertThrows(ValidationException.class, () -> service.getAllByBooker(2L, "UNKNOWN"));
    }

    @Test
    void getAllByOwner_shouldCoverAllStatesAndDefault() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerId(eq(1L), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(eq(1L), any(), any(), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerIdAndEndBefore(eq(1L), any(), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerIdAndStartAfter(eq(1L), any(), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerIdAndStatus(eq(1L), any(), any(Sort.class))).thenReturn(List.of());

        assertDoesNotThrow(() -> service.getAllByOwner(1L, "ALL"));
        assertDoesNotThrow(() -> service.getAllByOwner(1L, "CURRENT"));
        assertDoesNotThrow(() -> service.getAllByOwner(1L, "PAST"));
        assertDoesNotThrow(() -> service.getAllByOwner(1L, "FUTURE"));
        assertDoesNotThrow(() -> service.getAllByOwner(1L, "WAITING"));
        assertDoesNotThrow(() -> service.getAllByOwner(1L, "REJECTED"));
        assertThrows(ValidationException.class, () -> service.getAllByOwner(1L, "UNKNOWN"));
    }

    private Booking waitingBooking() {
        return Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

    private BookingDto request(Long itemId, int startDays, int endDays) {
        BookingDto dto = new BookingDto();
        dto.setItemId(itemId);
        dto.setStart(LocalDateTime.now().plusDays(startDays));
        dto.setEnd(LocalDateTime.now().plusDays(endDays));
        return dto;
    }
}
