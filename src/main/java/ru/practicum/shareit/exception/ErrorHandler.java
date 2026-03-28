package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> catchNotFoundStatus(NotFoundException ex) {
        log.error("404: {}", ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> catchConflictStatus(ConflictException ex) {
        log.error("409: {}", ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> catchIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("409 Integrity: {}", ex.getMessage());
        return Map.of("error", "Конфликт данных: такое значение уже существует");
    }

    @ExceptionHandler({
            ValidationException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> catchValidationErrors(Exception ex) {
        log.error("400 Validation: {}", ex.getMessage());
        String info = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException) {
            info = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        return Map.of("error", info != null ? info : "Ошибка валидации");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> catchTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if ("state".equals(ex.getName())) {
            String val = ex.getValue() != null ? ex.getValue().toString() : "UNKNOWN";
            log.error("400 State Error: {}", val);
            return Map.of("error", "Unknown state: " + val);
        }
        log.error("400 Mismatch: {}", ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> catchMissingHeader(MissingRequestHeaderException ex) {
        log.error("400 Header: {}", ex.getMessage());
        return Map.of("error", "Пропущен заголовок: " + ex.getHeaderName());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> catchAllExceptions(Throwable ex) {
        log.error("500: ", ex);
        return Map.of(
                "error", "Непредвиденная ошибка",
                "message", ex.getMessage() != null ? ex.getMessage() : "Описание отсутствует"
        );
    }
}