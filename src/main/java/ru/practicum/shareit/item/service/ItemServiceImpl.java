package ru.practicum.shareit.item.service;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenOperationException;
import ru.practicum.shareit.exception.MissingFieldException;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);
    private final List<Item> items = new ArrayList<>();
    private Long idCounter = 1L;
    @Autowired
    private final UserService userService;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        log.info("Запрос на создание предмета пользователем с ID: {}", userId);
        if (userService.getUserById(userId) == null) {
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        validateItemFields(itemDto);
        Item item = ItemMapper.toItem(itemDto, userId);
        item.setId(idCounter++);
        items.add(item);
        log.info("Создан предмет с ID: {} для пользователя с ID: {}", item.getId(), userId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto) {
        log.info("Запрос на обновление предмета с ID: {} пользователем с ID: {}", itemId, userId);
        Item foundItem = null;
        for (Item item : items) {
            if (item.getId().equals(itemId)) {
                foundItem = item;
                break;
            }
        }
        if (foundItem == null) {
            throw new ResourceNotFoundException("Предмет с ID " + itemId + " не найден.");
        }
        if (!foundItem.getOwner().equals(userId)) {
            throw new ForbiddenOperationException("Пользователь с ID " + userId + " не является владельцем предмета.");
        }

        log.info("Поля ItemDto для обновления: id = {}, name = {}, description = {}, available = {}, requestId = {}",
                foundItem.getId() != null ? foundItem.getId() : "пустое",
                foundItem.getName() != null && !foundItem.getName().isBlank() ? foundItem.getName() : "пустое",
                foundItem.getDescription() != null && !foundItem.getDescription().isBlank() ? foundItem.getDescription() : "пустое",
                foundItem.getAvailable() != null ? foundItem.getAvailable() : "пустое");

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            log.debug("Обновление имени предмета с ID: {} на {}", itemId, itemDto.getName());
            foundItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            log.debug("Обновление описания предмета с ID: {} на {}", itemId, itemDto.getDescription());
            foundItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null || !foundItem.getAvailable()) {
            log.debug("Обновление доступности предмета с ID: {} на {}", itemId, itemDto.getAvailable());
            foundItem.setAvailable(itemDto.getAvailable());
        }

        log.info("Успешно обновлен предмет с ID: {}", itemId);
        return ItemMapper.toItemDto(foundItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        log.info("Запрос на получение предмета с ID: {}", itemId);
        for (Item item : items) {
            if (item.getId().equals(itemId)) {
                log.info("Предмет с ID: {} найден", itemId);
                return ItemMapper.toItemDto(item);
            }
        }
        throw new IllegalArgumentException("Предмет не найден");
    }

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        log.info("Запрос на получение всех предметов пользователя с ID: {}", userId);
        List<ItemDto> result = new ArrayList<>();
        for (Item item : items) {
            if (item.getOwner().equals(userId)) {
                result.add(ItemMapper.toItemDto(item));
            }
        }
        log.info("Найдено {} предметов для пользователя с ID: {}", result.size(), userId);
        return result;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Запрос на поиск предметов по тексту: {}", text);
        List<ItemDto> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            log.warn("Пустой или отсутствующий текст поиска");
            return result;
        }

        String searchText = text.toLowerCase();

        for (Item item : items) {
            log.debug("Проверяем предмет: id={}, name={}, available={}", item.getId(), item.getName(), item.getAvailable());
            if (Boolean.TRUE.equals(item.getAvailable()) &&
                    (item.getName().toLowerCase().contains(searchText) ||
                            (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))) {
                log.debug("Предмет добавлен в результат поиска: id={}, name={}", item.getId(), item.getName());
                result.add(ItemMapper.toItemDto(item));
            }
        }
        log.info("Поиск завершен, найдено {} предметов", result.size());
        return result;
    }

    private void validateItemFields(ItemDto itemDto) {
        if (itemDto.getName().isEmpty()) {
            throw new MissingFieldException("Поле 'name' обязательно для заполнения.");
        }
        if (itemDto.getDescription().isEmpty() || itemDto.getDescription() == null) {
            throw new MissingFieldException("Поле 'description' обязательно для заполнения.");
        }
        if (itemDto.getAvailable() == null) {
            throw new MissingFieldException("Поле 'available' должно быть true.");
        }
    }
}
