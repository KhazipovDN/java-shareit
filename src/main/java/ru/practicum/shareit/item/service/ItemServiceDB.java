package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositary.BookingRepository;
import ru.practicum.shareit.booking.storage.BookingMapper;
import ru.practicum.shareit.exception.MissingFieldException;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.BookerInfoDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserRepository;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Primary
public class ItemServiceDB implements ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemServiceDB.class);
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        log.info("Создание предмета с записью в базу данных для пользователя с ID {}", userId);
        if (itemDto.getName() == null || itemDto.getDescription() == null
                || itemDto.getName().trim().isEmpty()
                || itemDto.getDescription().trim().isEmpty()) {
            log.error("Поле 'name/description' не может быть пустым.");
            throw new MissingFieldException("Поле 'name' не может быть пустым.");
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.error("Пользователь с ID {} не найден.", userId);
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        User user = userOptional.get();
        UserDto userDto = UserMapper.toUserDto(user);
        Item item = ItemMapper.toItem(itemDto, userDto);
        itemRepository.save(item);
        log.info("Предмет с ID {} успешно создан и сохранен в базе данных.", item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto) {
        log.info("Обновление предмета с ID {} для пользователя с ID {}", itemId, userId);
        Item existingItem = itemRepository.findByOwnerIdAndId(userId, itemId);;
        if (existingItem == null) {
            throw new ResourceNotFoundException("Предмет с ID " + itemId + " не найден");
        }
        User owner = existingItem.getOwner();
        if (owner == null || owner.getId() == null || !owner.getId().equals(userId)) {
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не является владельцем");
        }
        String newName = itemDto.getName();
        if (newName != null && !newName.isEmpty()) {
            existingItem.setName(newName);
        }
        String newDescription = itemDto.getDescription();
        if (newDescription != null && !newDescription.isEmpty()) {
            existingItem.setDescription(newDescription);
        }
        Boolean newAvailable = itemDto.getAvailable();
        if (newAvailable != null) {
            existingItem.setAvailable(newAvailable);
        }
        Item updatedItem = itemRepository.save(existingItem);
        log.info("Предмет с ID {} успешно обновлен", itemId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional
    public ItemDto getItemById(Long itemId) {
        log.info("Получение предмета с ID {}", itemId);
        Item foundItem = itemRepository.findById(itemId).orElse(null);
        if (foundItem == null) {
            log.info("Предмет с ID {} не найден.", itemId);
            throw new ResourceNotFoundException("Предмет с ID " + itemId + " не найден.");
        }
        List<CommentDto> comments = getAllCommentsByItemId(itemId);
        log.info("Предмет с ID {} успешно получен", itemId);
        return ItemMapper.toItemDto(foundItem, comments);
    }

    @Override
    @Transactional
    public ItemDto getByItemIdAndUserId(Long itemId, Long userId) {
        log.info("Получение предмета с ID {} для пользователя с ID {}", itemId, userId);
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (!optionalItem.isPresent()) {
            log.info("Предмет с ID {} не найден.", itemId);
            throw new ResourceNotFoundException("Предмет с ID " + itemId + " не найден.");
        }
        Item item = optionalItem.get();
        log.info("Предмет с ID {} успешно получен", itemId);
        if (item.getOwner().getId().equals(userId)) {
            log.info("Пользователь с ID {} является владельцем предмета с ID {}", userId, itemId);
            return ItemMapper.toItemDto(item, getLastBooking(item), getNextBooking(item), getAllCommentsByItemId(itemId));
        }

        log.info("Пользователь с ID {} не является владельцем предмета с ID {}", userId, itemId);
        return ItemMapper.toItemDto(item, getAllCommentsByItemId(itemId));
    }

    @Override
    @Transactional
    public List<ItemDto> getUserItems(Long userId) {
        log.info("Получение всех предметов пользователя с ID {} из базы данных.", userId);
        List<Item> userItems = itemRepository.findAllByOwnerId(userId);;
        if (userItems.isEmpty()) {
            log.info("В базе данных нет предметов для пользователя с ID {}.", userId);
            return new ArrayList<>();
        }
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : userItems) {
            itemDtos.add(ItemMapper.toItemDto(item));
        }
        return itemDtos;
    }

    @Override
    @Transactional
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск предметов по тексту: {}", text);
        if (text == null || text.trim().isEmpty()) {
            log.info("Пустой поисковый запрос - возврат пустого списка");
            return Collections.emptyList();
        }
        String searchText = "%" + text.trim().toUpperCase() + "%";
        List<Item> foundItems = itemRepository.search(searchText);
        List<ItemDto> result = new ArrayList<>();
        for (Item item : foundItems) {
            result.add(ItemMapper.toItemDto(item));
        }
        log.info("Найдено предметов: {}", result.size());
        return result;
    }

    @Override
    @Transactional
    public List<CommentDto> getAllCommentsByItemId(Long itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        List<CommentDto> commentDtos = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDto commentDto = CommentMapper.toCommentDto(comment);
            commentDtos.add(commentDto);
        }
        return commentDtos;
    }

    @Override
    @Transactional
    public CommentDto createComment(CommentDto commentDto, UserDto userDto, ItemDto itemDto) {
        if (userDto == null || itemDto == null) {
            throw new ResourceNotFoundException("Пользователь или предмет не найдены");
        }
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new MissingFieldException("Текст комментария не может быть пустым");
        }
        List<Booking> bookings = bookingRepository
                .getAllUserBookings(userDto.getId(), itemDto.getId(), LocalDateTime.now());

        if (bookings.isEmpty()) {
            throw new MissingFieldException("Чтобы оставить комментарий нужно сначала оформить бронирование");
        }
        Comment comment = CommentMapper.toComment(commentDto, userDto, itemDto);
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }

    private BookerInfoDto getLastBooking(Item item) {
        Optional<Booking> lastBookingOptional = bookingRepository.getLastBooking(item.getId(), LocalDateTime.now());
        if (lastBookingOptional.isPresent()) {
            Booking lastBooking = lastBookingOptional.get();
            return BookingMapper.toBookingInfoDto(lastBooking);
        }
        return null;
    }

    private BookerInfoDto getNextBooking(Item item) {
        Optional<Booking> nextBookingOptional = bookingRepository.getNextBooking(item.getId(), LocalDateTime.now());
        if (nextBookingOptional.isPresent()) {
            Booking nextBooking = nextBookingOptional.get();
            return BookingMapper.toBookingInfoDto(nextBooking);
        }
        return null;
    }
}