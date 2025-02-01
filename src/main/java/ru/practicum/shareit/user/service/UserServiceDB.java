package ru.practicum.shareit.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.SameEmailException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Primary
public class UserServiceDB implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceDB.class);
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto.getEmail());
        validateUser(userDto);
        User user = UserMapper.toUser(userDto);
        User createdUser = userRepository.save(user);
        log.info("Пользователь создан: {}", createdUser.getEmail());
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Обновление пользователя с ID: {}", userId);
        validateUser(userDto);
        Optional<User> existingUserOptional = userRepository.findById(userId);
        if (existingUserOptional.isEmpty()) {
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (userDto.getEmail() != null) {
            boolean emailExists = userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId);
            if (emailExists) {
                throw new SameEmailException("Email " + userDto.getEmail() + " уже используется другим пользователем.");
            }
        }
        User existingUser = existingUserOptional.get();
        User updatedUser = userRepository.save(existingUser);
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        log.info("Пользователь обновлен: {}", updatedUser.getId());
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя по ID: {}", userId);

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        log.info("Пользователь найден: {}", user);
        return UserMapper.toUserDto(user.orElse(null));
    }

    @Override
    @Transactional
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        List<User> users = userRepository.findAll();
        log.info("Найдено {} пользователей", users.size());
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с ID {} не найден для удаления", userId);
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        userRepository.deleteById(userId);
        log.info("Пользователь с ID {} успешно удален", userId);
    }

    private void validateUser(UserDto userDto) {
        if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), userDto.getId())) {
            log.error("Адрес электронной почты {} уже используется другим пользователем", userDto.getEmail());
            throw new SameEmailException("Email уже используется другим пользователем");
        }
    }
}