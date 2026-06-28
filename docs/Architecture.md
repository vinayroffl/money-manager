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

This will be written in Java Springboot.

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

Postgresql will be the Database for this project

## Authentication

We will be using JWT based authorization.

## Deployment

We will use Docker for Deployment

## Future Scalability
