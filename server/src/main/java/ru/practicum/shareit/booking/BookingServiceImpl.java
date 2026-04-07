package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingStorage;
    private final UserRepository userStorage;
    private final ItemRepository itemStorage;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingDto dto) {
        User requester = fetchUser(userId);
        Item targetItem = fetchItem(dto.getItemId());

        if (!targetItem.getAvailable()) {
            throw new ValidationException("Предмет с id=" + targetItem.getId() + " сейчас недоступен");
        }
        if (targetItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать собственный предмет");
        }
        verifyBookingDates(dto);

        Booking entity = BookingMapper.toBooking(dto);
        entity.setItem(targetItem);
        entity.setBooker(requester);
        entity.setStatus(BookingStatus.WAITING);

        return BookingMapper.toBookingDto(bookingStorage.save(entity));
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking currentBooking = fetchBooking(bookingId);

        if (!currentBooking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Только владелец может менять статус бронирования");
        }
        if (currentBooking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус данного бронирования уже определен");
        }

        currentBooking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingStorage.save(currentBooking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking entity = fetchBooking(bookingId);

        boolean isBooker = entity.getBooker().getId().equals(userId);
        boolean isOwner = entity.getItem().getOwner().getId().equals(userId);

        if (!(isBooker || isOwner)) {
            throw new NotFoundException("Информация о бронировании недоступна для пользователя " + userId);
        }
        return BookingMapper.toBookingDto(entity);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId, String stateStr) {
        fetchUser(userId);
        LocalDateTime pointInTime = LocalDateTime.now();
        Sort descStart = Sort.by(Sort.Direction.DESC, "start");

        List<Booking> result;
        switch (stateStr.toUpperCase()) {
            case "ALL":
                result = bookingStorage.findAllByBookerId(userId, descStart);
                break;
            case "CURRENT":
                result = bookingStorage.findAllByBookerIdAndStartBeforeAndEndAfter(userId, pointInTime, pointInTime, descStart);
                break;
            case "PAST":
                result = bookingStorage.findAllByBookerIdAndEndBefore(userId, pointInTime, descStart);
                break;
            case "FUTURE":
                result = bookingStorage.findAllByBookerIdAndStartAfter(userId, pointInTime, descStart);
                break;
            case "WAITING":
                result = bookingStorage.findAllByBookerIdAndStatus(userId, BookingStatus.WAITING, descStart);
                break;
            case "REJECTED":
                result = bookingStorage.findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED, descStart);
                break;
            default:
                throw new ValidationException("Unknown state: " + stateStr);
        }
        return result.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwner(Long userId, String stateStr) {
        fetchUser(userId);
        LocalDateTime pointInTime = LocalDateTime.now();
        Sort descStart = Sort.by(Sort.Direction.DESC, "start");

        List<Booking> result;
        switch (stateStr.toUpperCase()) {
            case "ALL":
                result = bookingStorage.findAllByItemOwnerId(userId, descStart);
                break;
            case "CURRENT":
                result = bookingStorage.findAllByItemOwnerIdAndStartBeforeAndEndAfter(userId, pointInTime, pointInTime, descStart);
                break;
            case "PAST":
                result = bookingStorage.findAllByItemOwnerIdAndEndBefore(userId, pointInTime, descStart);
                break;
            case "FUTURE":
                result = bookingStorage.findAllByItemOwnerIdAndStartAfter(userId, pointInTime, descStart);
                break;
            case "WAITING":
                result = bookingStorage.findAllByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, descStart);
                break;
            case "REJECTED":
                result = bookingStorage.findAllByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, descStart);
                break;
            default:
                throw new ValidationException("Unknown state: " + stateStr);
        }
        return result.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private User fetchUser(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    private Item fetchItem(Long id) {
        return itemStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Предмет не найден: " + id));
    }

    private Booking fetchBooking(Long id) {
        return bookingStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + id));
    }

    private void verifyBookingDates(BookingDto dto) {
        LocalDateTime limit = LocalDateTime.now();
        if (dto.getStart().isBefore(limit.minusSeconds(1))) {
            throw new ValidationException("Начало бронирования не может быть в прошлом");
        }
        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().isEqual(dto.getStart())) {
            throw new ValidationException("Окончание должно быть строго после начала");
        }
    }
}