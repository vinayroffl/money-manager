type TransactionType = "INCOME" | "EXPENSE";

interface TransactionRequest {
  amount: number;
  type: TransactionType;
  categoryId: number;
  description?: string;
  transactionDate?: string;
}

interface TransactionResponse {
  id: string;
  amount: number;
  type: TransactionType;
  categoryId: number;
  categoryName: string;
  description?: string;
  transactionDate?: string;
  createdAt: string;
  updatedAt: string;
}

type CategoryResponse = {
  id: number;
  name: string;
  description: string;
  transactionType: TransactionType;
};

export type {
  TransactionType,
  TransactionRequest,
  TransactionResponse,
  CategoryResponse,
};
