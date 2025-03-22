package ru.practicum.shareit.item.service;


import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItemById(Long itemId);

    ItemDto getByItemIdAndUserId(Long itemId, Long userId);

    List<ItemDto> getUserItems(Long userId);

    List<ItemDto> searchItems(String text);

    List<CommentDto> getAllCommentsByItemId(Long itemId);

    CommentDto createComment(CommentDto commentDto, UserDto userDto, ItemDto itemDto);
}