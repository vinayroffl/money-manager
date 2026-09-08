package com.vinay.moneymanager.transaction.dto.response;

import com.vinay.moneymanager.transaction.entity.TransactionType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
  private Integer id;
  private String name;
  private String description;
  private TransactionType transactionType;
}
