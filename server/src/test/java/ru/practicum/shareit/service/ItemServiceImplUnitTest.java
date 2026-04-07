package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplUnitTest {
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

    private ItemServiceImpl service;
    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        service = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository, requestRepository);
        owner = User.builder().id(1L).name("owner").email("o@mail.com").build();
        item = Item.builder().id(1L).name("Drill").description("d").available(true).owner(owner).build();
    }

    @Test
    void create_shouldThrowWhenRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(requestRepository.existsById(10L)).thenReturn(false);
        ItemDto dto = new ItemDto();
        dto.setName("x");
        dto.setDescription("y");
        dto.setAvailable(true);
        dto.setRequestId(10L);

        assertThrows(NotFoundException.class, () -> service.create(1L, dto));
    }

    @Test
    void update_shouldThrowForNotOwner() {
        User other = User.builder().id(2L).name("other").email("x@mail.com").build();
        Item foreignOwned = Item.builder()
                .id(1L)
                .name("Drill")
                .description("d")
                .available(true)
                .owner(other)
                .build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(foreignOwned));

        assertThrows(NotFoundException.class, () -> service.update(1L, 1L, new ItemDto()));
    }

    @Test
    void search_shouldReturnEmptyOnBlank() {
        assertTrue(service.search(" ").isEmpty());
        assertTrue(service.search(null).isEmpty());
    }

    @Test
    void getById_shouldSetBookingsForOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(1L)).thenReturn(List.of());
        Booking booking = Booking.builder()
                .id(5L)
                .item(item)
                .booker(owner)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        when(bookingRepository.findAllByItemIdAndStatusNot(eq(1L), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(booking));

        ItemDto dto = service.getById(1L, 1L);

        assertNotNull(dto.getNextBooking());
    }

    @Test
    void createComment_shouldThrowWhenNoApprovedBooking() {
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(eq(1L), eq(1L), any(), eq(BookingStatus.APPROVED)))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> service.createComment(1L, 1L, comment("x")));
    }

    private CommentDto comment(String text) {
        CommentDto dto = new CommentDto();
        dto.setText(text);
        return dto;
    }
}
