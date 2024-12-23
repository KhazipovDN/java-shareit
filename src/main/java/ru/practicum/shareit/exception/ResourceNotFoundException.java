package ru.practicum.shareit.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message, String details) {
        super(message + ": " + details);
    }
}