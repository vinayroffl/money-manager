package com.vinay.moneymanager.common.exception;

import com.vinay.moneymanager.common.response.ApiError;
import com.vinay.moneymanager.common.response.ValidationError;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  public static final ZoneId ZONE_ID_INDIA = ZoneId.of("Asia/Kolkata");

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleResourceNotFoundException(
      ResourceNotFoundException exception) {
    return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiError> handleDuplicateResourceException(
      DuplicateResourceException exception) {
    return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiError> handleInvalidCredentialsException(
      InvalidCredentialsException exception) {
    return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentialsException() {
    return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password");
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> handleAuthenticationException() {
    return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication required");
  }

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<ApiError> handleInvalidRequestException(InvalidRequestException exception) {
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(FeatureNotImplementedException.class)
  public ResponseEntity<ApiError> handleFeatureNotImplementedException(
      FeatureNotImplementedException exception) {
    return buildResponse(HttpStatus.NOT_IMPLEMENTED, exception.getMessage());
  }

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
            .timestamp(now())
            .errors(validationErrors)
            .build();

    return ResponseEntity.badRequest().body(apiError);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException exception) {
    String expectedType =
        exception.getRequiredType() == null
            ? "the expected type"
            : exception.getRequiredType().getSimpleName();
    return buildResponse(
        HttpStatus.BAD_REQUEST,
        "Invalid value for parameter '%s'. Expected type: %s"
            .formatted(exception.getName(), expectedType));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleHttpMessageNotReadableException() {
    return buildResponse(HttpStatus.BAD_REQUEST, "Request body is missing or malformed");
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiError> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException exception) {
    return buildResponse(
        HttpStatus.BAD_REQUEST,
        "Missing required parameter '%s'".formatted(exception.getParameterName()));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiError> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException exception) {
    return buildResponse(
        HttpStatus.METHOD_NOT_ALLOWED,
        "HTTP method '%s' is not supported for this endpoint".formatted(exception.getMethod()));
  }

  @ExceptionHandler(ServletRequestBindingException.class)
  public ResponseEntity<ApiError> handleServletRequestBindingException() {
    return buildResponse(HttpStatus.BAD_REQUEST, "Required request value is missing");
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiError> handleHttpMediaTypeNotSupportedException() {
    return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type");
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<ApiError> handleHttpMediaTypeNotAcceptableException() {
    return buildResponse(HttpStatus.NOT_ACCEPTABLE, "Requested media type is not supported");
  }

  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<ApiError> handleNotFoundException(Exception exception) {
    return buildResponse(HttpStatus.NOT_FOUND, "Resource not found");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleDataIntegrityViolationException(
      DataIntegrityViolationException exception) {
    log.warn("Data integrity violation", exception);
    return buildResponse(HttpStatus.CONFLICT, "Request conflicts with existing data");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDeniedException() {
    return buildResponse(HttpStatus.FORBIDDEN, "Access denied");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpectedException(Exception exception) {
    log.error("Unhandled exception", exception);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
  }

  private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message) {
    ApiError apiError =
        ApiError.builder().message(message).timestamp(now()).build();
    return ResponseEntity.status(status).body(apiError);
  }

  private LocalDateTime now() {
    return LocalDateTime.now(ZONE_ID_INDIA);
  }
}
