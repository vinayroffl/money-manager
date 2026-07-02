package com.vinay.moneymanager.common.exception;

import com.vinay.moneymanager.common.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleException(Exception exception) {
        ApiError apiError = new ApiError();
        apiError.setMessage(exception.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    // DuplicateResourceException
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleException(DuplicateResourceException exception) {
        ApiError apiError = new ApiError();
        apiError.setMessage(exception.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    // InvalidCredentialsException
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentialsException(InvalidCredentialsException e) {
        ApiError apiError = new ApiError();
        apiError.setMessage(e.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

}
