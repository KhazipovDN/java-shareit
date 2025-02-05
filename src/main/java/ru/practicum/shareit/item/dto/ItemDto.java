package ru.practicum.shareit.item.dto;

import lombok.*;
/**
 * TODO Sprint add-controllers.
 */

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}

