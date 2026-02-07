package com.awesomepizza.order.exception.handler;

import com.awesomepizza.order.exception.InvalidOrderStatusException;
import com.awesomepizza.order.exception.OrderModificationNotAllowedException;
import com.awesomepizza.order.exception.OrderNotFoundException;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
    }

    @Test
    @DisplayName("Should handle OrderNotFoundException and return 404")
    void shouldHandleOrderNotFoundException() {
        // Given
        OrderNotFoundException exception = new OrderNotFoundException("Order ORD-123 not found");

        // When
        ResponseEntity<?> response = exceptionHandler.handleOrderNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Should handle OrderModificationNotAllowedException and return 400")
    void shouldHandleOrderModificationNotAllowedException() {
        // Given
        OrderModificationNotAllowedException exception = 
            new OrderModificationNotAllowedException("Order cannot be modified");

        // When
        ResponseEntity<?> response = exceptionHandler.handleOrderModificationNotAllowedException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Should handle InvalidOrderStatusException and return 400")
    void shouldHandleInvalidOrderStatusException() {
        // Given
        InvalidOrderStatusException exception = 
            new InvalidOrderStatusException("Invalid status transition");

        // When
        ResponseEntity<?> response = exceptionHandler.handleInvalidOrderStatusException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Should handle OptimisticLockException and return 409 CONFLICT")
    void shouldHandleOptimisticLockException() {
        // Given
        OptimisticLockException exception = new OptimisticLockException("Optimistic lock failed");

        // When
        ResponseEntity<?> response = exceptionHandler.handleOptimisticLockException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException and return validation errors")
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        
        FieldError fieldError1 = new FieldError("order", "customerName", "Il nome è obbligatorio");
        FieldError fieldError2 = new FieldError("order", "phone", "Formato telefono non valido");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get("customerName")).isEqualTo("Il nome è obbligatorio");
        assertThat(response.getBody().get("phone")).isEqualTo("Formato telefono non valido");
    }
}