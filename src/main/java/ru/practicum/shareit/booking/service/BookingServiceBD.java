package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceBD implements BookingService {

    private final BookingRepository bookingRepository;
    private static final String BOOKING_NOT_FOUND_MESSAGE = "Бронирования с id %s нет";

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
            throw new ResourceNotFoundException("Такой вещи нет");
        }
        Booking booking = BookingMapper.toBooking(bookingInputDto, BookingStatus.WAITING, itemDto, userDto);
        return BookingMapper.toBookingCreatedDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveByOwner(Long userId, Long bookingId, Boolean approved) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new ResourceNotFoundException("Бронирование отсутсвует");
        }

        Booking booking = bookingOptional.get();
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new ForbiddenOperationException("У пользователя нет такой вещи");
        }

        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ResourceNotFoundException("Статус уже поставлен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toBookingCreatedDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingByIdAndUser(Long bookingId, Long userId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new ResourceNotFoundException("Бронирование отсутсвует");
        }

        Booking booking = bookingOptional.get();

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
                throw new IncorrectStateException("Unknown state: " + state);
        }
    }
}