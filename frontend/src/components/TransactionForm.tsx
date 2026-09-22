import { useEffect, useState } from "react";
import type {
  CategoryResponse,
  TransactionRequest,
  TransactionResponse,
  TransactionType,
} from "../types/transaction";
import {
  createTransaction,
  getCategories,
  updateTransaction,
} from "../api/transactionApi";
import ApiError from "../api/ApiError";

interface TransactionFormProps {
  transaction: TransactionResponse | null;
  onSuccess: () => Promise<void>;
  onCancel: () => void;
}

function TransactionForm({
  transaction,
  onSuccess,
  onCancel,
}: TransactionFormProps) {
  const [type, setType] = useState<TransactionType | "">(
    transaction?.type ?? "",
  );

  const [amount, setAmount] = useState(
    transaction ? String(transaction.amount) : "",
  );

  const [categoryId, setCategoryId] = useState(
    transaction ? String(transaction.categoryId) : "",
  );

  const [date, setDate] = useState(
    transaction?.transactionDate?.slice(0, 16) ?? "",
  );

  const [description, setDescription] = useState(
    transaction?.description ?? "",
  );

  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const loadCategories = async () => {
      try {
        setErrorMessage("");

        const response = await getCategories();

        if (response.success) {
          setCategories(response.data);
        }
      } catch (error) {
        if (error instanceof ApiError) {
          setErrorMessage(error.message);
        } else {
          setErrorMessage("Unable to load categories.");
        }
      }
    };

    void loadCategories();
  }, []);



  const filteredCategories = categories.filter(
    (category) => category.transactionType === type,
  );

  const handleSubmit = async () => {
    try {
      setErrorMessage("");

      if (!type || !categoryId || !amount) {
        return;
      }

      setIsSubmitting(true);

      const request: TransactionRequest = {
        amount: Number(amount),
        type,
        categoryId: Number(categoryId),
        description: description.trim() || undefined,
        transactionDate: date || undefined,
      };

      if (transaction) {
        await updateTransaction(transaction.id, request);
      } else {
        await createTransaction(request);
      }

      await onSuccess();
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage(
          transaction
            ? "Unable to update transaction."
            : "Unable to create transaction.",
        );
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form
      className="transaction-form"
      onSubmit={(event) => {
        event.preventDefault();
        void handleSubmit();
      }}
    >
      {errorMessage && <p>{errorMessage}</p>}

      <div className="form-field">
        <label htmlFor="type">Type</label>

        <select
          id="type"
          value={type}
          onChange={(event) => {
            setType(event.target.value as TransactionType);
            setCategoryId("");
          }}
          required
          disabled={isSubmitting}
        >
          <option value="">Select type</option>
          <option value="EXPENSE">Expense</option>
          <option value="INCOME">Income</option>
        </select>
      </div>

      <div className="form-field">
        <label htmlFor="category">Category</label>

        <select
          id="category"
          value={categoryId}
          onChange={(event) => setCategoryId(event.target.value)}
          disabled={!type || isSubmitting}
          required
        >
          <option value="">Select category</option>

          {filteredCategories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </div>

      <div className="form-field">
        <label htmlFor="amount">Amount</label>

        <input
          id="amount"
          type="number"
          min="0.01"
          step="0.01"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          required
          disabled={isSubmitting}
        />
      </div>

      <div className="form-field">
        <label htmlFor="date">Date</label>

        <input
          id="date"
          type="datetime-local"
          value={date}
          onChange={(event) => setDate(event.target.value)}
          disabled={isSubmitting}
        />
      </div>

      <div className="form-field">
        <label htmlFor="description">Description</label>

        <textarea
          id="description"
          maxLength={500}
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          disabled={isSubmitting}
        />
      </div>

      <div className="transaction-form-actions">
        <button
          className="transaction-cancel-button"
          type="button"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          Cancel
        </button>

        <button
          className="transaction-submit-button"
          type="submit"
          disabled={isSubmitting}
        >
          {isSubmitting
            ? transaction
              ? "Updating..."
              : "Saving..."
            : transaction
              ? "Update Transaction"
              : "Save Transaction"}
        </button>
      </div>
    </form>
  );
}

export default TransactionForm;
