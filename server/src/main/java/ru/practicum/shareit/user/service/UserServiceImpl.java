package ru.practicum.shareit.user.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.SameEmailException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserStorage userStorage;


    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);
        validateUser(userDto);
        User user = UserMapper.toUser(userDto);
        User createdUser = userStorage.addUser(user);
        log.debug("Пользователь создан: {}", createdUser);
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Обновление пользователя с идентификатором: {}", userId);
        validateUser(userDto);
        User updatedUser = userStorage.updateUser(userId, UserMapper.toUser(userDto));
        if (updatedUser == null) {
            log.error("Пользователь с идентификатором {} не найден для обновления", userId);
            throw new ResourceNotFoundException("Пользователь не найден");
        }
        log.info("Пользователь обновлен: {}", updatedUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя по идентификатору: {}", userId);
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден");
        }
        log.debug("Пользователь найден: {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        List<UserDto> result = new ArrayList<>();
        Map<Long, User> users = userStorage.getAllUsers();
        for (User user : users.values()) {
            result.add(UserMapper.toUserDto(user));
        }
        log.debug("Всего получено пользователей: {}", result.size());
        return result;
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с идентификатором: {}", userId);
        boolean deleted = userStorage.deleteUser(userId);
        if (!deleted) {
            log.error("Пользователь с идентификатором {} не найден для удаления", userId);
            throw new ResourceNotFoundException("Пользователь не найден");
        }
        log.info("Пользователь с идентификатором {} успешно удален", userId);
    }

    private void validateUser(UserDto userDto) {
        if (userStorage.emailExists(userDto.getEmail(), userDto.getId())) {
            log.error("Адрес электронной почты {} уже существует для другого пользователя", userDto.getEmail());
            throw new SameEmailException("Email уже используется другим пользователем");
        }
    }
}