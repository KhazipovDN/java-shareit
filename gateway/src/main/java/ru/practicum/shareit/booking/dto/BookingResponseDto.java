package ru.practicum.shareit.booking.dto;

import lombok.Value;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Value
public class BookingResponseDto {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    State status;
    UserDto booker;
    ItemDto item;
}
