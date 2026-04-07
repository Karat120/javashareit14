package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

@UtilityClass
public class BookingMapper {
    public static Booking toBooking(BookingDto dto) {
        return Booking.builder()
                .id(dto.getId())
                .start(dto.getStart())
                .end(dto.getEnd())
                .build();
    }

    public static BookingDto toBookingDto(Booking model) {
        return BookingDto.builder()
                .id(model.getId())
                .start(model.getStart())
                .end(model.getEnd())
                .status(model.getStatus())
                .itemId(model.getItem().getId())
                .item(ItemMapper.toItemDto(model.getItem()))
                .booker(UserMapper.toUserDto(model.getBooker()))
                .build();
    }
}