package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long id;
    private String text;
    private Item item;

    private User author;
    private LocalDateTime created;
}
