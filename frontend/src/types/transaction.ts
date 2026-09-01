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

export type { TransactionType, TransactionRequest, TransactionResponse };
