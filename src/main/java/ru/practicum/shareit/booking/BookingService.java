package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import java.util.List;

public interface BookingService {
    List<BookingDto> getAllByOwner(Long userId, String state);

    List<BookingDto> getAllByBooker(Long userId, String state);

    BookingDto getById(Long userId, Long bookingId);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto create(Long userId, BookingDto bookingDto);
}