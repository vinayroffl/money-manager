package com.vinay.moneymanager.common.exception;

import com.vinay.moneymanager.common.response.ApiError;
import com.vinay.moneymanager.common.response.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleException(ResourceNotFoundException exception) {
        ApiError apiError = ApiError.builder().message(exception.getMessage()).timestamp(LocalDateTime.now()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    // DuplicateResourceException
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleException(DuplicateResourceException exception) {
        ApiError apiError = ApiError.builder().message(exception.getMessage()).timestamp(LocalDateTime.now()).build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    // InvalidCredentialsException
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentialsException(InvalidCredentialsException exception) {
        ApiError apiError = ApiError.builder().message(exception.getMessage()).timestamp(LocalDateTime.now()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    // MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

        List<ValidationError> validationErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError ->
                        ValidationError.builder()
                                .field(fieldError.getField())
                                .message(fieldError.getDefaultMessage())
                                .build())
                .toList();
        ApiError apiError = ApiError.builder()
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .errors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(apiError);
    }

}
