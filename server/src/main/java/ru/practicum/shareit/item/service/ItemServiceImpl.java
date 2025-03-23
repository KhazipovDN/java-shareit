package ru.practicum.shareit.item.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenOperationException;
import ru.practicum.shareit.exception.MissingFieldException;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);
    private final ItemStorage itemStorage;
    private final UserService userService;


    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        log.info("Создание предмета для пользователя с ID {}", userId);
        userService.getUserById(userId);
        validateItemFields(itemDto);
        System.out.println("Номер бронирования - "+itemDto.getRequestId());
        Item item = ItemMapper.toItem(itemDto, userService.getUserById(userId));
        itemStorage.addItem(item);
        log.info("Предмет с ID {} успешно создан для пользователя {}", item.getId(), userId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto) {
        log.info("Обновление предмета с ID {} для пользователя с ID {}", itemId, userId);
        Item foundItem = itemStorage.getItemById(itemId);
        if (foundItem == null) {
            log.info("Предмет с ID {} не найден.", itemId);
            throw new ResourceNotFoundException("Предмет с ID " + itemId + " не найден.");
        }
        if (!foundItem.getOwner().getId().equals(userId)) {
            log.info("Пользователь с ID {} не является владельцем предмета с ID {}", userId, itemId);
            throw new ForbiddenOperationException("Пользователь с ID " + userId + " не является владельцем предмета.");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            foundItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            foundItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            foundItem.setAvailable(itemDto.getAvailable());
        }
        log.info("Предмет с ID {} успешно обновлен", itemId);
        itemStorage.updateItem(foundItem);
        return ItemMapper.toItemDto(foundItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        log.info("Получение предмета с ID {}", itemId);
        Item foundItem = itemStorage.getItemById(itemId);
        if (foundItem == null) {
            log.info("Предмет с ID {} не найден.", itemId);
            throw new ResourceNotFoundException("Предмет с ID " + itemId + " не найден.");
        }
        log.info("Предмет с ID {} успешно получен", itemId);
        return ItemMapper.toItemDto(foundItem);
    }

    @Override
    public ItemDto getByItemIdAndUserId(Long itemId, Long userId) {
        return null;
    }

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        log.info("Получение предметов пользователя с ID {}", userId);
        userService.getUserById(userId);
        List<ItemDto> userItems = new ArrayList<>();
        List<Item> allItems = itemStorage.getAllItems();
        for (Item item : allItems) {
            if (item.getOwner().getId().equals(userId)) {
                userItems.add(ItemMapper.toItemDto(item));
            }
        }
        log.debug("Найдено {} предметов для пользователя с ID {}", userItems.size(), userId);
        return userItems;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск предметов с текстом: {}", text);
        List<ItemDto> searchResults = new ArrayList<>();
        if (text == null || text.isBlank()) {
            log.debug("Текст для поиска пустой или null.");
            return searchResults;
        }
        String searchText = text.toLowerCase();
        List<Item> allItems = itemStorage.getAllItems();
        for (Item item : allItems) {
            if (Boolean.TRUE.equals(item.getAvailable()) &&
                    (item.getName().toLowerCase().contains(searchText) ||
                            (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))) {
                searchResults.add(ItemMapper.toItemDto(item));
            }
        }
        log.debug("Найдено {} предметов по запросу: {}", searchResults.size(), text);
        return searchResults;
    }

    @Override
    public List<CommentDto> getAllCommentsByItemId(Long itemId) {
        return List.of();
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, UserDto userDto, ItemDto itemDto) {
        return null;
    }

    private void validateItemFields(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            throw new MissingFieldException("Поле 'name' обязательно для заполнения.");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isEmpty()) {
            throw new MissingFieldException("Поле 'description' обязательно для заполнения.");
        }
        if (itemDto.getAvailable() == null) {
            throw new MissingFieldException("Поле 'available' должно быть хоть как - то заполнено");
        }
    }
}