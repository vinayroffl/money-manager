import { useEffect, useState } from "react";
import type {
  CategoryResponse,
  TransactionRequest,
  TransactionType,
} from "../types/transaction";
import { createTransaction, getCategories } from "../api/transactionApi";
import ApiError from "../api/ApiError";

interface TransactionFormProps {
  onTransactionCreated: () => Promise<void>;
  onCancel: () => void;
}
function TransactionForm({
  onTransactionCreated,
  onCancel,
}: TransactionFormProps) {
  const [type, setType] = useState<TransactionType | "">("");
  const [amount, setAmount] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [date, setDate] = useState("");
  const [description, setDescription] = useState("");
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

      await createTransaction(request);

      setType("");
      setAmount("");
      setCategoryId("");
      setDate("");
      setDescription("");

      await onTransactionCreated();
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Unable to create transaction.");
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
          disabled={!type}
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
        />
      </div>

      <div className="form-field">
        <label htmlFor="date">Date</label>

        <input
          id="date"
          type="datetime-local"
          value={date}
          onChange={(event) => setDate(event.target.value)}
        />
      </div>

      <div className="form-field">
        <label htmlFor="description">Description</label>

        <textarea
          id="description"
          maxLength={500}
          value={description}
          onChange={(event) => setDescription(event.target.value)}
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
          {isSubmitting ? "Saving..." : "Save Transaction"}
        </button>
      </div>
    </form>
  );
}

export default TransactionForm;
