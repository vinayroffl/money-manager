package com.vinay.moneymanager.common.exception;

import com.vinay.moneymanager.common.response.ApiError;
import com.vinay.moneymanager.common.response.ValidationError;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  public static final ZoneId ZONE_ID_INDIA = ZoneId.of("Asia/Kolkata");

  // ResourceNotFoundException
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleException(ResourceNotFoundException exception) {
    ApiError apiError =
        ApiError.builder()
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now(ZONE_ID_INDIA))
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
  }

  // DuplicateResourceException
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiError> handleException(DuplicateResourceException exception) {
    ApiError apiError =
        ApiError.builder()
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now(ZONE_ID_INDIA))
            .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
  }

  // InvalidCredentialsException
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiError> handleInvalidCredentialsException(
      InvalidCredentialsException exception) {
    ApiError apiError =
        ApiError.builder()
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now(ZONE_ID_INDIA))
            .build();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
  }

  // MethodArgumentNotValidException
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception) {

    List<ValidationError> validationErrors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    ValidationError.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build())
            .toList();
    ApiError apiError =
        ApiError.builder()
            .message("Validation failed")
            .timestamp(LocalDateTime.now(ZONE_ID_INDIA))
            .errors(validationErrors)
            .build();

    return ResponseEntity.badRequest().body(apiError);
  }

  @ExceptionHandler(FeatureNotImplementedException.class)
  public ResponseEntity<ApiError> handleFeatureNotImplementedException(
      FeatureNotImplementedException exception) {
    ApiError apiError =
        ApiError.builder()
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now(ZONE_ID_INDIA))
            .build();
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(apiError);
  }

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<ApiError> handleInvalidRequestException(InvalidRequestException exception) {
    ApiError apiError =
        ApiError.builder()
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now(ZONE_ID_INDIA))
            .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
  }
}
