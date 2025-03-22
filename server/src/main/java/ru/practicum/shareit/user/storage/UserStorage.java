package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Map;

public interface UserStorage {
    User addUser(User user);

    User getUserById(Long userId);

    User updateUser(Long userId, User updatedUser);

    boolean deleteUser(Long userId);

    Map<Long, User> getAllUsers();

    boolean emailExists(String email, Long userId);
}
