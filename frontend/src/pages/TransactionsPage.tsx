import { useEffect, useState } from "react";
import AppLayout from "../components/AppLayout";
import { getTransactions } from "../api/transactionApi";
import type { TransactionResponse } from "../types/transaction";
import ApiError from "../api/ApiError";

function TransactionsPage() {
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  useEffect(() => {
    const loadTransactions = async () => {
      try {
        setIsLoading(true);
        setErrorMessage("");

        const response = await getTransactions();

        if (response.success) {
          setTransactions(response.data);
        }
      } catch (error) {
        if (error instanceof ApiError) {
          setErrorMessage(error.message);
        } else {
          setErrorMessage("Something went wrong. Please try again.");
        }
      } finally {
        setIsLoading(false);
      }
    };

    void loadTransactions();
  }, []);
  return (
    <AppLayout>
      <div>
        <h2>Transactions</h2>
        <p>Manage your income and expenses.</p>

        {isLoading && <p>Loading transactions...</p>}

        {errorMessage && <p>{errorMessage}</p>}

        {!isLoading && !errorMessage && transactions.length === 0 && (
          <p>No transactions yet.</p>
        )}

        {!isLoading && transactions.length > 0 && (
          <p>
            You have {transactions.length} transaction
            {transactions.length === 1 ? "" : "s"}.
          </p>
        )}
      </div>
    </AppLayout>
  );
}

export default TransactionsPage;
