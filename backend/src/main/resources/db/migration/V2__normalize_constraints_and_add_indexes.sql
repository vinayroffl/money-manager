-- V2: Normalize generated constraint names and add indexes.
--
-- V1 (or a baselined legacy database) still has Hibernate-generated hash names.
-- Renaming them here makes both fresh and existing databases converge on the
-- same, readable schema.

ALTER TABLE users
    RENAME CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 TO uk_users_email;

ALTER TABLE categories
    RENAME CONSTRAINT ukt8o6pivur7nn124jehx7cygw5 TO uk_categories_name;

ALTER TABLE transactions
    RENAME CONSTRAINT fkqwv7rmvc8va8rep7piikrojds TO fk_transactions_user;

ALTER TABLE transactions
    RENAME CONSTRAINT fksqqi7sneo04kast0o138h19mv TO fk_transactions_category;

ALTER TABLE budgets
    RENAME CONSTRAINT fkln0tm5tgf3f9q3sp9sa5m8m7b TO fk_budgets_user;

ALTER TABLE budgets
    RENAME CONSTRAINT fkn7qib00712y8dwelmqfwis6ka TO fk_budgets_category;

-- Transaction list and month filters.
CREATE INDEX idx_transactions_user_date
    ON transactions (user_id, transaction_date);

-- Budget "spent amount" aggregation and category filters.
CREATE INDEX idx_transactions_user_category_date
    ON transactions (user_id, category_id, transaction_date);

-- Transaction type filters.
CREATE INDEX idx_transactions_user_type
    ON transactions (user_id, type);

-- Monthly budget lookups.
CREATE INDEX idx_budgets_user_year_month
    ON budgets (user_id, budget_year, budget_month);

-- Foreign-key lookup / delete support.
CREATE INDEX idx_budgets_category
    ON budgets (category_id);
