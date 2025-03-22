package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 1L;

    @Override
    public Item addItem(Item item) {
        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public boolean updateItem(Item updatedItem) {
        Long itemId = updatedItem.getId();
        if (items.containsKey(itemId)) {
            items.put(itemId, updatedItem);
            return true;
        }
        return false;
    }

    @Override
    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<>();
        for (Item item : items.values()) {
            itemList.add(item);
        }
        return itemList;
    }

    @Override
    public Item getItemById(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public boolean deleteItem(Long itemId) {
        return items.remove(itemId) != null;
    }

}