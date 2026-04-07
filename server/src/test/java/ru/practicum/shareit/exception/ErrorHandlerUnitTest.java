package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErrorHandlerUnitTest {
    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void shouldHandleNotFound() {
        assertEquals("not found", handler.catchNotFoundStatus(new NotFoundException("not found")).get("error"));
    }

    @Test
    void shouldHandleConflict() {
        assertEquals("conflict", handler.catchConflictStatus(new ConflictException("conflict")).get("error"));
    }

    @Test
    void shouldHandleIntegrityViolation() {
        assertEquals("Конфликт данных: такое значение уже существует",
                handler.catchIntegrityViolation(new DataIntegrityViolationException("x")).get("error"));
    }

    @Test
    void shouldHandleValidationException() {
        assertEquals("bad", handler.catchValidationErrors(new ValidationException("bad")).get("error"));
    }

    @Test
    void shouldHandleTypeMismatch() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("BAD", String.class, "state", null, new RuntimeException());
        assertEquals("Unknown state: BAD", handler.catchTypeMismatch(ex).get("error"));
    }

    @Test
    void shouldHandleMissingHeader() {
        MissingRequestHeaderException ex = mock(MissingRequestHeaderException.class);
        when(ex.getHeaderName()).thenReturn("X-Sharer-User-Id");
        assertEquals("Пропущен заголовок: X-Sharer-User-Id", handler.catchMissingHeader(ex).get("error"));
    }
}
