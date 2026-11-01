# Money Manager by Vinay

[![CI](https://github.com/vinayroffl/money-manager/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/vinayroffl/money-manager/actions/workflows/ci.yml)

A modern full-stack personal finance management application built with Java, Spring Boot, React and PostgreSQL.

## About

Money manager is a web application which lets you manage your day to day finances. Income and Expense tracking, Budgeting, Lending Tracking, Loan Tracking, Investments Tracking, etc.

## Features

- Sign up using email and password
- Sign in using email and password
- Log out
- Home Page Dashboard with Expense, Income, Budgeting, Lending, Loan, Investments, with High level insights of each currently only income and expense dashboard will be available other features are developed in a later stage.
- Transaction Dashboard which Shows Monthly transactions in a simple UI
- Add transactions
- Update existing transactions
- Delete transactions
- Expense Dashboard
- Income Dashboard
- Lending Dashboard - coming soon
- Loan Dashboard - coming soon
- Investments Dashboard - coming soon

## Tech Stack

### Backend

- Java 21 (LTS)
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- Maven (via Maven Wrapper)

### Frontend

- React
- TypeScript
- Vite
- React Router
- TanStack Query (later)
- Material UI

### Database

- PostgreSQL

### Authentication

- JWT

### ORM

- Hibernate / JPA

### Testing

- JUnit 5
- Mockito
- Vitest
- React Testing Library

### DevOps

- Docker
- Docker Compose
- GitHub Actions

### IDE

- IntelliJ IDEA.
- VS Code

## Project Status

- 28-06-2026 - Initialized.

## Roadmap

### Phase 1 — Foundation

- Planning
- Git
- Architecture
- Project setup
- Docker
- PostgreSQL

### Phase 2 — Authentication

- Signup
- Login
- JWT
- Security

### Phase 3 — Transactions

- CRUD
- Categories
- Accounts

### Phase 4 — Dashboard

- Charts
- Monthly summaries
- Budget overview

### Phase 5 — Budgets

### Phase 6 — Lending

### Phase 7 — Loans

### Phase 8 — Investments

### Phase 9 — Deployment

- CI/CD
- Production Docker
- Reverse Proxy
- SSL
- Cloud deployment

## Screenshots (Coming Soon)

## Folder Structure

```MoneyManager/

    backend/

    frontend/

    docs/

    docker/

    database/

    scripts/

    .gitignore

    README.md
```

## How to Run

See [docs/Configuration.md](docs/Configuration.md) for the required environment variables
and local setup instructions.

Backend tests run against a Testcontainers-managed PostgreSQL 15 database, so Docker
must be running before you execute `./mvnw verify`. The tests apply the real Flyway
migrations and validate the JPA entities against the resulting schema.

## Continuous Integration

GitHub Actions runs on every push and pull request:

- **Backend** — `./mvnw verify` on Java 21 (unit and integration tests against
  Testcontainers PostgreSQL).
- **Frontend** — `npm ci`, `npm run lint`, and `npm run build` on Node.js 22.
- **Backend image** — builds the multi-stage Docker image from `backend/` and checks
  that it starts Java 21.
- **Database migrations** — applies every Flyway migration to a throwaway PostgreSQL 15
  database and checks that the schema reaches the latest version.

The workflow is defined in [.github/workflows/ci.yml](.github/workflows/ci.yml).

## License

MIT License

Copyright (c) 2026 Vinay R

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
