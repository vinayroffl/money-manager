import { useEffect, useState } from "react";
import AppLayout from "../components/AppLayout";
import TransactionForm from "../components/TransactionForm";
import { getTransactions, deleteTransaction } from "../api/transactionApi";
import type {
  TransactionResponse,
  TransactionType,
} from "../types/transaction";
import ApiError from "../api/ApiError";
import { ChevronDown, Pencil, Trash2 } from "lucide-react";

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
  const [selectedTransaction, setSelectedTransaction] =
    useState<TransactionResponse | null>(null);
  const [transactionToDelete, setTransactionToDelete] =
    useState<TransactionResponse | null>(null);
  const [selectedCategory, setSelectedCategory] = useState("ALL");
  const [selectedType, setSelectedType] = useState<"ALL" | TransactionType>(
    "ALL",
  );
  const [filterMenuOpen, setFilterMenuOpen] = useState<
    "type" | "category" | null
  >(null);

  const loadTransactions = async () => {
    try {
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
    let ignore = false;

    const fetchInitialTransactions = async () => {
      try {
        const response = await getTransactions();

        if (ignore) {
          return;
        }

        if (!response.success) {
          setErrorMessage(response.message || "Unable to load transactions.");
          return;
        }

        const sortedTransactions = [...response.data].sort(
          (a, b) =>
            new Date(b.transactionDate ?? b.createdAt).getTime() -
            new Date(a.transactionDate ?? a.createdAt).getTime(),
        );

        setTransactions(sortedTransactions);
      } catch (error) {
        if (ignore) {
          return;
        }

        setErrorMessage(
          error instanceof ApiError
            ? error.message
            : "Unable to load transactions. Please try again.",
        );
      } finally {
        if (!ignore) {
          setIsLoading(false);
        }
      }
    };

    void fetchInitialTransactions();

    return () => {
      ignore = true;
    };
  }, []);

  const handleDelete = async () => {
    if (!transactionToDelete) {
      return;
    }

    try {
      await deleteTransaction(transactionToDelete.id);

      await loadTransactions();

      setTransactionToDelete(null);
      setSuccessMessage("Transaction deleted successfully.");

      setTimeout(() => {
        setSuccessMessage("");
      }, 3000);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Unable to delete transaction.");
      }
    }
  };

  const uniqueCategories = Array.from(
    new Map(
      transactions.map((transaction) => [
        transaction.categoryId,
        {
          id: transaction.categoryId,
          name: transaction.categoryName,
        },
      ]),
    ).values(),
  );

  const filteredTransactions = transactions.filter((transaction) => {
    const matchesType =
      selectedType === "ALL" || transaction.type === selectedType;

    const matchesCategory =
      selectedCategory === "ALL" ||
      transaction.categoryName === selectedCategory;

    return matchesType && matchesCategory;
  });

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
            onClick={() => {
              setSelectedTransaction(null);
              setIsFormOpen(true);
            }}
          >
            + Add Transaction
          </button>
        </div>

        {isFormOpen && (
          <div className="modal-backdrop">
            <div className="modal-card">
              <h3>
                {selectedTransaction ? "Edit Transaction" : "Add Transaction"}
              </h3>

              <TransactionForm
                key={selectedTransaction?.id ?? "new"}
                transaction={selectedTransaction}
                onSuccess={async () => {
                  await loadTransactions();

                  setSuccessMessage(
                    selectedTransaction
                      ? "Transaction updated successfully."
                      : "Transaction added successfully.",
                  );

                  setIsFormOpen(false);
                  setSelectedTransaction(null);

                  setTimeout(() => {
                    setSuccessMessage("");
                  }, 3000);
                }}
                onCancel={() => {
                  setIsFormOpen(false);
                  setSelectedTransaction(null);
                }}
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

        {transactionToDelete && (
          <div className="modal-backdrop">
            <div className="modal-card">
              <h3>Delete Transaction</h3>

              <p>Are you sure you want to delete this transaction?</p>

              <div className="transaction-form-actions">
                <button
                  className="transaction-cancel-button"
                  type="button"
                  onClick={() => setTransactionToDelete(null)}
                >
                  Cancel
                </button>

                <button
                  className="delete-confirm-button"
                  type="button"
                  onClick={() => {
                    void handleDelete();
                  }}
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        )}

        {!isLoading && !errorMessage && transactions.length > 0 && (
          <table className="transactions-table">
            <thead>
              <tr>
                <th>Date</th>
                <th className="table-filter-header">
                  <button
                    className="table-filter-trigger"
                    aria-expanded={filterMenuOpen === "type"}
                    onClick={() =>
                      setFilterMenuOpen(
                        filterMenuOpen === "type" ? null : "type",
                      )
                    }
                  >
                    <span>Type</span>
                    <ChevronDown size={18} />
                  </button>

                  {filterMenuOpen === "type" && (
                    <div className="table-filter-menu">
                      <button
                        type="button"
                        aria-pressed={selectedType === "ALL"}
                        onClick={() => {
                          setSelectedType("ALL");
                          setFilterMenuOpen(null);
                        }}
                      >
                        All
                      </button>

                      <button
                        type="button"
                        aria-pressed={selectedType === "INCOME"}
                        onClick={() => {
                          setSelectedType("INCOME");
                          setFilterMenuOpen(null);
                        }}
                      >
                        Income
                      </button>

                      <button
                        type="button"
                        aria-pressed={selectedType === "EXPENSE"}
                        onClick={() => {
                          setSelectedType("EXPENSE");
                          setFilterMenuOpen(null);
                        }}
                      >
                        Expense
                      </button>
                    </div>
                  )}
                </th>
                <th className="table-filter-header">
                  <button
                    className="table-filter-trigger"
                    aria-expanded={filterMenuOpen === "category"}
                    onClick={() =>
                      setFilterMenuOpen(
                        filterMenuOpen === "category" ? null : "category",
                      )
                    }
                  >
                    <span>Category</span>
                    <ChevronDown size={18} />
                  </button>

                  {filterMenuOpen === "category" && (
                    <div className="table-filter-menu">
                      <button
                        key="ALL"
                        type="button"
                        aria-pressed={selectedCategory === "ALL"}
                        onClick={() => {
                          setSelectedCategory("ALL");
                          setFilterMenuOpen(null);
                        }}
                      >
                        All
                      </button>
                      {uniqueCategories.map((category) => (
                        <button
                          key={category.id}
                          type="button"
                          aria-pressed={selectedCategory === category.name}
                          onClick={() => {
                            setSelectedCategory(category.name);
                            setFilterMenuOpen(null);
                          }}
                        >
                          {category.name}
                        </button>
                      ))}
                    </div>
                  )}
                </th>
                <th>Description</th>
                <th>Actions</th>
                <th className="amount-column">Amount</th>
              </tr>
            </thead>

            <tbody>
              {filteredTransactions.length === 0 && (
                <tr>
                  <td colSpan={6} className="transactions-empty-state">
                    No transactions match your filters.
                  </td>
                </tr>
              )}
              {filteredTransactions.map((transaction) => (
                <tr key={transaction.id}>
                  <td>{formatTransactionDate(transaction.transactionDate)}</td>

                  <td
                    className={`transaction-type ${transaction.type.toLowerCase()}`}
                  >
                    {transaction.type === "INCOME" ? "Income" : "Expense"}
                  </td>

                  <td>{transaction.categoryName}</td>

                  <td>{transaction.description || "-"}</td>

                  <td>
                    <div className="transaction-actions">
                      <button
                        className="transaction-action-button"
                        type="button"
                        title="Edit transaction"
                        onClick={() => {
                          setSelectedTransaction(transaction);
                          setIsFormOpen(true);
                        }}
                      >
                        <Pencil size={18} />
                      </button>

                      <button
                        className="transaction-action-button delete"
                        type="button"
                        title="Delete transaction"
                        onClick={() => setTransactionToDelete(transaction)}
                      >
                        <Trash2 size={18} />
                      </button>
                    </div>
                  </td>

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
