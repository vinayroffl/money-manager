import { useEffect, useState } from "react";
import AppLayout from "../components/AppLayout";
import TransactionForm from "../components/TransactionForm";
import { getTransactions } from "../api/transactionApi";
import type { TransactionResponse } from "../types/transaction";
import ApiError from "../api/ApiError";

function formatTransactionDate(date?: string) {
  if (!date) {
    return "-";
  }

  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function formatTransactionAmount(amount: number) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
  }).format(amount);
}

function TransactionsPage() {
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  const loadTransactions = async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const response = await getTransactions();

      if (response.success) {
        const sortedTransactions = [...response.data].sort(
          (a, b) =>
            new Date(b.transactionDate ?? b.createdAt).getTime() -
            new Date(a.transactionDate ?? a.createdAt).getTime(),
        );

        setTransactions(sortedTransactions);
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

  useEffect(() => {
    void loadTransactions();
  }, []);

  return (
    <AppLayout>
      <div>
        <div className="transactions-header">
          <div>
            <h2>Transactions</h2>
            <p>Manage your income and expenses.</p>
          </div>

          <button
            className="add-transaction-button"
            type="button"
            onClick={() => setIsFormOpen(true)}
          >
            + Add Transaction
          </button>
        </div>

        {isFormOpen && (
          <div className="modal-backdrop">
            <div className="modal-card">
              <h3>Add Transaction</h3>
              <TransactionForm
                onTransactionCreated={async () => {
                  await loadTransactions();
                  setSuccessMessage("Transaction added successfully.");
                  setIsFormOpen(false);
                  setTimeout(() => {
                    setSuccessMessage("");
                  }, 3000);
                }}
                onCancel={() => setIsFormOpen(false)}
              />
            </div>
          </div>
        )}

        {successMessage && (
          <div className="toast-message">{successMessage}</div>
        )}
        {isLoading && <p>Loading transactions...</p>}

        {errorMessage && <p>{errorMessage}</p>}

        {!isLoading && !errorMessage && transactions.length === 0 && (
          <p>No transactions yet.</p>
        )}

        {!isLoading && !errorMessage && transactions.length > 0 && (
          <table className="transactions-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Type</th>
                <th>Category</th>
                <th>Description</th>
                <th className="amount-column">Amount</th>
              </tr>
            </thead>

            <tbody>
              {transactions.map((transaction) => (
                <tr key={transaction.id}>
                  <td>{formatTransactionDate(transaction.transactionDate)}</td>

                  <td
                    className={`transaction-type ${transaction.type.toLowerCase()}`}
                  >
                    {transaction.type === "INCOME" ? "Income" : "Expense"}
                  </td>

                  <td>{transaction.categoryName}</td>

                  <td>{transaction.description || "-"}</td>

                  <td className="amount-column">
                    {formatTransactionAmount(transaction.amount)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </AppLayout>
  );
}

export default TransactionsPage;
