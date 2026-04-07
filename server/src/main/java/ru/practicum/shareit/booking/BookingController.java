package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @RequestParam(defaultValue = "ALL") String state) {
        log.info("Fetching bookings for owner ID: {}, state: {}", ownerId, state);
        return bookingService.getAllByOwner(ownerId, state);
    }

    @GetMapping
    public List<BookingDto> getBookerBookings(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                              @RequestParam(defaultValue = "ALL") String state) {
        log.info("Fetching bookings for booker ID: {}, state: {}", bookerId, state);
        return bookingService.getAllByBooker(bookerId, state);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto setApproval(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @PathVariable Long bookingId,
                                  @RequestParam Boolean approved) {
        log.info("Updating approval for booking ID: {} by user: {}", bookingId, userId);
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long bookingId) {
        log.info("Requesting booking details for ID: {} by user: {}", bookingId, userId);
        return bookingService.getById(userId, bookingId);
    }

    @PostMapping
    public BookingDto saveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @RequestBody BookingDto dto) {
        log.info("Creating new booking request from user ID: {}", userId);
        return bookingService.create(userId, dto);
    }
}