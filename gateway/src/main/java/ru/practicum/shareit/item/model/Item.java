package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private Long requestId;
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();
}

