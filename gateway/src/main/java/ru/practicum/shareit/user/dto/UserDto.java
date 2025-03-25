package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import lombok.*;
import ru.practicum.shareit.Update;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;

    @NotBlank(groups = {Default.class, Update.class})
    private String name;
    @NotBlank(groups = Default.class)
    @Email(groups = {Default.class, Update.class})
    private String email;

}
