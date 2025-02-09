package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositary.BookingRepository;
import ru.practicum.shareit.booking.storage.BookingMapper;
import ru.practicum.shareit.exception.ForbiddenOperationException;
import ru.practicum.shareit.exception.IncorrectStateException;
import ru.practicum.shareit.exception.MissingFieldException;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceBD implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;

    @Override
    @Transactional
    public BookingResponseDto create(UserDto userDto, ItemDto itemDto, BookingCreateDto bookingInputDto) {
        if (!itemDto.getAvailable()) {
            throw new MissingFieldException("Вещь недоступна для бронирования");
        }
        if (bookingInputDto.getEnd().isBefore(bookingInputDto.getStart())) {
            throw new MissingFieldException("Дата окончания не может быть раньше даты начала");
        }
        if (bookingInputDto.getStart().isBefore(LocalDateTime.now())) {
            throw new MissingFieldException("Дата начала не может быть раньше текущей даты");
        }
        if (Objects.equals(itemDto.getOwnerId(), userDto.getId())) {
            throw new ResourceNotFoundException("Нельзя забронировать свою собственную вещь");
        }
        Booking booking = BookingMapper.toBooking(bookingInputDto, BookingStatus.WAITING, itemDto, userDto);
        return BookingMapper.toBookingCreatedDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveByOwner(Long userId, Long bookingId, Boolean approved) {
        userService.getUserById(userId);;
        Booking booking = findBookingById(bookingId);
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new ForbiddenOperationException("Пользователь с id " + userId +
                    " не является владельцем вещи, связанной с бронированием id " + bookingId);
        }

        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ResourceNotFoundException("Статус уже поставлен для бронирования с id " + bookingId);
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toBookingCreatedDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingByIdAndUser(Long bookingId, Long userId) {
        userService.getUserById(userId);;
        Booking booking = findBookingById(bookingId);

        if (!Objects.equals(booking.getBooker().getId(), userId)
                && !Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new ResourceNotFoundException("Такого бронирования нет");
        }

        return BookingMapper.toBookingCreatedDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> findAllByBooker(Long bookerId, State state) {
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllBookingsByBookerId(bookerId));
            case CURRENT:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllCurrentBookingsByBookerId(bookerId, now));
            case WAITING:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllWaitingBookingsByBookerId(bookerId, now));
            case PAST:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllPastBookingsByBookerId(bookerId, now));
            case FUTURE:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllFutureBookingsByBookerId(bookerId, now));
            case REJECTED:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllRejectedBookingsByBookerId(bookerId));
            default:
                throw new IncorrectStateException("Неизвестный статус: " + state);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> findAllByOwner(Long userId, State state) {
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllBookingsByOwnerId(userId));
            case CURRENT:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllCurrentBookingsByOwnerId(userId, now));
            case WAITING:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllWaitingBookingsByOwnerId(userId, now));
            case PAST:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllPastBookingsByOwnerId(userId, now));
            case FUTURE:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllFutureBookingsByOwnerId(userId, now));
            case REJECTED:
                return BookingMapper
                        .toBookingCreatedDto(bookingRepository
                                .getAllRejectedBookingsByOwnerId(userId));
            default:
                throw new IncorrectStateException("Неизвестный статус: " + state);
        }
    }

    private Booking findBookingById(Long bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        Booking booking = bookingOptional.get();
        if (booking == null) {
            throw new ResourceNotFoundException("Бронирования нет с id " + bookingId);
        }
        return booking;
    }
}