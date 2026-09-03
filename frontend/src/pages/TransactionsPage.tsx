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

        {!isLoading && transactions.length >= 0 && (
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
                  <td>{transaction.description}</td>
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
export default TransactionsPage;
