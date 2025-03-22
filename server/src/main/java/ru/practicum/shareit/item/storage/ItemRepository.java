package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i " +
            "where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%')))" +
            "AND i.available = TRUE")
    List<Item> search(String text);

    @Query("select i from Item i where i.owner.id = ?1")
    List<Item> findAllByOwnerId(Long ownerId);

    Item findByOwnerIdAndId(Long ownerId, Long id);

    List<Item> findByRequestId(Long requestId);

}
