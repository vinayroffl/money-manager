package com.vinay.moneymanager.common.response;

import lombok.Data;

@Data
public class ValidationError {
    private String field;
    private String message;
}
