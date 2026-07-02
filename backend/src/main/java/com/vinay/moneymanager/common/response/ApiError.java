package com.vinay.moneymanager.common.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApiError {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private List<ValidationError> errors;
}
