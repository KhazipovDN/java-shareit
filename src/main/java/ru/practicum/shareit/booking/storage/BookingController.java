package ru.practicum.shareit.booking.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    public BookingController(@Qualifier("userServiceDB") UserService userService,
                             @Qualifier("itemServiceDB")ItemService itemService,
                             BookingService bookingService) {
        this.userService = userService;
        this.itemService = itemService;
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @Validated({Create.class}) @RequestBody BookingCreateDto bookingInputDto) {
        UserDto userDto = userService.getUserById(userId);
        ItemDto itemDto = itemService.getItemById(bookingInputDto.getItemId());
        return bookingService.create(userDto, itemDto, bookingInputDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam("approved") Boolean approved,
                                           @PathVariable("bookingId") Long bookingId) {
        return bookingService.approveByOwner(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable("bookingId") Long bookingId) {
        return bookingService.getBookingByIdAndUser(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(value = "state", defaultValue = "ALL") State state) {
        UserDto userDto = userService.getUserById(userId);

        return bookingService.findAllByBooker(userDto.getId(), state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(value = "state", defaultValue = "ALL") State state) {
        UserDto userDto = userService.getUserById(userId);

        return bookingService.findAllByOwner(userDto.getId(), state);
    }
}
