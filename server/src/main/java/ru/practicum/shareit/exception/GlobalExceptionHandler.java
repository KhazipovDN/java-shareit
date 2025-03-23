package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SameEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleSameEmailException(final SameEmailException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(final ResourceNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(MissingFieldException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingFieldException(MissingFieldException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenOperationException(ForbiddenOperationException e) {
        return new ErrorResponse(e.getMessage());
    }
}