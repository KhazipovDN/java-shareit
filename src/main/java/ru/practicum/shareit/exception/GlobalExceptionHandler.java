package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(java.lang.IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public java.lang.IllegalArgumentException handleResourceNotFoundException(java.lang.IllegalArgumentException ex) {
        return new java.lang.IllegalArgumentException("Ошибка: ресурс не найден");
    }

    @ExceptionHandler(SameEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public SameEmailException handleResourceNotSameEmailException(SameEmailException ex) {
        return new SameEmailException(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResourceNotFoundException handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResourceNotFoundException("Ресурс не найден", ex.getMessage());
    }

    @ExceptionHandler(MissingFieldException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MissingFieldException handleResourceNotFoundException(MissingFieldException ex) {
        return new MissingFieldException("Ошибка проверки полей");
    }


    @ExceptionHandler(ForbiddenOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ForbiddenOperationException handleResourceNotFoundException(ForbiddenOperationException ex) {
        return new ForbiddenOperationException("Ошибка проверки полей");
    }

}