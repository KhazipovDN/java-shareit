package ru.practicum.shareit.user.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.SameEmailException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final List<User> users = new ArrayList<>();
    private Long idCounter = 1L;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Creating user with data: {}", userDto);
        validateUser(userDto);
        User user = UserMapper.toUser(userDto);
        user.setId(idCounter++);
        users.add(user);
        log.debug("User created: {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Updating user with ID: {}", userId);
        validateUser(userDto);
        for (User user : users) {
            if (user.getId().equals(userId)) {
                if (userDto.getName() != null) {
                    log.debug("Updating user name from '{}' to '{}'", user.getName(), userDto.getName());
                    user.setName(userDto.getName());
                }
                if (userDto.getEmail() != null) {
                    log.debug("Updating user email from '{}' to '{}'", user.getEmail(), userDto.getEmail());
                    user.setEmail(userDto.getEmail());
                }
                log.info("User updated: {}", user);
                return UserMapper.toUserDto(user);
            }
        }
        log.error("User with ID {} not found for update", userId);
        throw new IllegalArgumentException("User not found");
    }


    @Override
    public UserDto getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);
        for (User user : users) {
            if (user.getId().equals(userId)) {
                log.debug("User found: {}", user);
                return UserMapper.toUserDto(user);
            }
        }
        throw new ResourceNotFoundException("Ошибка", "Пользователь с ID " + userId + " не найден");
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");

        List<UserDto> result = new ArrayList<>();
        for (User user : users) {
            result.add(UserMapper.toUserDto(user));
        }

        log.debug("Total users fetched: {}", result.size());
        return result;
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        User userToRemove = null;
        for (User user : users) {
            if (user.getId().equals(userId)) {
                userToRemove = user;
                break;
            }
        }

        if (userToRemove != null) {
            users.remove(userToRemove);
            log.info("User with ID {} deleted successfully", userId);
        } else {
            log.error("User with ID {} not found for deletion", userId);
            throw new IllegalArgumentException("User not found");
        }
    }

    private void validateUser(UserDto userDto) {
        for (User user : users) {
            if (user.getEmail().equals(userDto.getEmail())) {
                log.error("Email {} already exists. Cannot create user.", userDto.getEmail());
                throw new SameEmailException("Email already exists");
            }
        }
    }


}