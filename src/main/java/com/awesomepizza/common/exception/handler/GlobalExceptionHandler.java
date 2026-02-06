package com.awesomepizza.common.exception.handler;

import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.awesomepizza.common.exception.InvalidOrderStatusException;
import com.awesomepizza.common.exception.OrderModificationNotAllowedException;
import com.awesomepizza.common.exception.OrderNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ExceptionDetails> handleOrderNotFoundException(OrderNotFoundException ex, WebRequest request) {
        ExceptionDetails details = new ExceptionDetails(
            LocalDateTime.now(),
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(details, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderModificationNotAllowedException.class)
    public ResponseEntity<ExceptionDetails> handleOrderModificationNotAllowedException(OrderModificationNotAllowedException ex, WebRequest request) {
        ExceptionDetails details = new ExceptionDetails(
            LocalDateTime.now(),
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(details, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ExceptionDetails> handleInvalidOrderStatusException(InvalidOrderStatusException ex, WebRequest request) {
        ExceptionDetails details = new ExceptionDetails(
            LocalDateTime.now(),
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(details, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ExceptionDetails> handleOptimisticLockException(OptimisticLockException ex, WebRequest request) {
        ExceptionDetails details = new ExceptionDetails(
            LocalDateTime.now(),
            "L'ordine è stato modificato da un altro utente. Ricarica i dati e riprova.",
            request.getDescription(false)
        );
        return new ResponseEntity<>(details, HttpStatus.CONFLICT);
    }

    // Helper class for consistent error response structure
    private record ExceptionDetails(
        LocalDateTime timestamp,
        String message,
        String details
    ) {}
}