package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private static final Logger log = LoggerFactory.getLogger(ItemRequestServiceImpl.class);
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestResponseDto createRequest(Long userId, ItemRequestDto itemRequestDto) {
        log.info("Создание запроса для пользователя с ID {}", userId);
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    throw new ResourceNotFoundException("User not found");
                });

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Запрос с ID {} успешно создан для пользователя с ID {}", savedRequest.getId(), userId);

        return ItemRequestMapper.toItemRequestResponseDto(savedRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя с ID {}", userId);
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    throw new ResourceNotFoundException("User not found");
                });

        List<ItemRequest> requests = itemRequestRepository.findByRequesterOrderByCreatedDesc(requester);
        List<ItemRequestResponseDto> responseDtos = new ArrayList<>();

        for (ItemRequest request : requests) {
            responseDtos.add(ItemRequestMapper.toItemRequestResponseDto(request));
        }

        log.info("Найдено {} запросов для пользователя с ID {}", responseDtos.size(), userId);
        return responseDtos;
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, int from, int size) {
        log.info("Получение всех запросов, начиная с {}, количество {}", from, size);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    throw new ResourceNotFoundException("User not found");
                });

        PageRequest page = PageRequest.of(from / size, size);
        List<ItemRequest> requests = itemRequestRepository.findByRequesterNotOrderByCreatedDesc(user, page);
        List<ItemRequestResponseDto> responseDtos = new ArrayList<>();

        for (ItemRequest request : requests) {
            responseDtos.add(ItemRequestMapper.toItemRequestResponseDto(request));
        }

        log.info("Найдено {} запросов", responseDtos.size());
        return responseDtos;
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        log.info("Получение запроса с ID {} для пользователя с ID {}", requestId, userId);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    throw new ResourceNotFoundException("User not found");
                });

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("Запрос с ID {} не найден", requestId);
                    throw new ResourceNotFoundException("Request not found");
                });

        List<Item> items = itemRepository.findByRequestId(requestId);
        ItemRequestResponseDto responseDto = ItemRequestMapper.toItemRequestResponseDto(request);
        responseDto.setItems(items);

        log.info("Запрос с ID {} успешно получен", requestId);
        return responseDto;
    }
}