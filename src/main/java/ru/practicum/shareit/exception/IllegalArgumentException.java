package ru.practicum.shareit.exception;

public class IllegalArgumentException extends RuntimeException {
    public IllegalArgumentException(String message, String exMessage) {
        super(message);
    }
}
