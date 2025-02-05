package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item addItem(Item item);

    boolean updateItem(Item updatedItem);

    List<Item> getAllItems();

    Item getItemById(Long itemId);

    boolean deleteItem(Long itemId);
}
