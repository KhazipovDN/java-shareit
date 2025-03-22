package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;


public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        return itemRequest;
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest) {
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(itemRequest.getId());
        responseDto.setDescription(itemRequest.getDescription());
        responseDto.setCreated(itemRequest.getCreated());
        responseDto.setItems(itemRequest.getItems());
        return responseDto;
    }
}