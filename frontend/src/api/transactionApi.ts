import type { ApiResponse } from "../types/api";
import type {
  CategoryResponse,
  TransactionRequest,
  TransactionResponse,
} from "../types/transaction";
import apiClient from "./apiClient";

async function getTransactions(): Promise<ApiResponse<TransactionResponse[]>> {
  const response = await apiClient("/api/transactions", {
    method: "GET",
  });

  const data: ApiResponse<TransactionResponse[]> = await response.json();
  return data;
}

async function getCategories(): Promise<ApiResponse<CategoryResponse[]>> {
  const response = await apiClient("/api/transactions/categories", {
    method: "GET",
  });

  const data: ApiResponse<CategoryResponse[]> = await response.json();
  return data;
}

async function createTransaction(
  request: TransactionRequest,
): Promise<ApiResponse<TransactionResponse>> {
  const response = await apiClient("/api/transactions", {
    method: "POST",
    body: JSON.stringify(request),
  });

  const data: ApiResponse<TransactionResponse> = await response.json();
  return data;
}
async function updateTransaction(
  id: string,
  request: TransactionRequest,
): Promise<ApiResponse<TransactionResponse>> {
  const response = await apiClient(`/api/transactions/${id}`, {
    method: "PUT",
    body: JSON.stringify(request),
  });

  const data: ApiResponse<TransactionResponse> = await response.json();
  return data;
}

async function deleteTransaction(id: string): Promise<void> {
  await apiClient(`/api/transactions/${id}`, {
    method: "DELETE",
  });
}

export {
  getTransactions,
  getCategories,
  createTransaction,
  updateTransaction,
  deleteTransaction,
};
