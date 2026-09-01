import type { ApiResponse } from "../types/api";
import type { TransactionResponse } from "../types/transaction";
import apiClient from "./apiClient";

async function getTransactions(): Promise<ApiResponse<TransactionResponse[]>> {
  const response = await apiClient("/api/transactions", {
    method: "GET",
  });

  const data: ApiResponse<TransactionResponse[]> = await response.json();
  return data;
}

export { getTransactions };
