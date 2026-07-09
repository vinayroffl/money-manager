# Architecture

```text
Browser
   │
React App
   │
REST API
(Spring Boot)
   │
PostgreSQL
```

## Architecture Style

This will be a modular monolithic application. As it is a small project and there is only a single developer. This allows aasier debugging, easier deployment and we can even evolve later.

## Backend

The backend will be built using Java 21 and Spring Boot 3.x.

```text
.
└── backend/
    └── src/
        └── main/
            └── java/
                └── com/vinay/moneymanager/
                    ├── config
                    ├── common
                    ├── auth
                    ├── transaction
                    ├── dashboard
                    ├── budget
                    ├── lending
                    ├── loan
                    ├── investment
                    └── user
```

## Frontend

This will be written in React

## Database

PostgreSQL will be the primary relational database.

## Authentication

We will be using JWT based authorization.

## Deployment

We will use Docker for Deployment

## Future Scalability
