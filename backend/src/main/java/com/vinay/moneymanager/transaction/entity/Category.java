package com.vinay.moneymanager.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String name;

  @NotBlank
  @Column(nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  private TransactionType transactionType;
}
