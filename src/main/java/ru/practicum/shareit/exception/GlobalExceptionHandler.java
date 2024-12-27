package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SameEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public SameEmailException handleSameEmailException(final SameEmailException e) {
        return new SameEmailException(e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResourceNotFoundException handleResourceNotFoundException(final ResourceNotFoundException e) {
        return new ResourceNotFoundException(e.getMessage());
    }

    @ExceptionHandler(MissingFieldException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MissingFieldException handleMissingFieldException(MissingFieldException e) {
        return new MissingFieldException(e.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ForbiddenOperationException handleForbiddenOperationException(ForbiddenOperationException e) {
        return new ForbiddenOperationException(e.getMessage());
    }
}