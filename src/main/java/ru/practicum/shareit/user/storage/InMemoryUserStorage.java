package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;

    @Override
    public User addUser(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        return users.get(userId);
    }

    @Override
    public User updateUser(Long userId, User updatedUser) {
        if (!users.containsKey(userId)) {
            return null;
        }
        User user = users.get(userId);
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        return user;
    }

    @Override
    public boolean deleteUser(Long userId) {
        return users.remove(userId) != null;
    }

    @Override
    public Map<Long, User> getAllUsers() {
        return users;
    }

    @Override
    public boolean emailExists(String email, Long userId) {
        for (User user : users.values()) {
            if (user.getEmail().equalsIgnoreCase(email) && !user.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }
}