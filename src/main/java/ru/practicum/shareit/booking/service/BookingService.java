package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(UserDto userDto, ItemDto itemDto, BookingCreateDto bookingInputDto);

    BookingResponseDto approveByOwner(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto getBookingByIdAndUser(Long bookingId, Long userId);

    List<BookingResponseDto> findAllByBooker(Long bookerId, State state);

    List<BookingResponseDto> findAllByOwner(Long userId, State state);
}
