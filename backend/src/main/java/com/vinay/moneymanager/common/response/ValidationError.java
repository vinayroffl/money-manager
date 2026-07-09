package com.vinay.moneymanager.common.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationError {
    private String field;
    private String message;
}
