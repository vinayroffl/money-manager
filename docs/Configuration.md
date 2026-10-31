# Configuration

All runtime secrets and environment-specific settings are supplied through environment
variables. Nothing sensitive is committed to the repository.

## Backend variables

| Variable | Required | Default | Description |
| --- | --- | --- | --- |
| `JWT_SECRET` | Yes | — | HS256 signing key. At least 32 bytes. Generate with `openssl rand -hex 32`. |
| `SPRING_PROFILES_ACTIVE` | No | `dev` | `dev`, `prod`, or `test`. |
| `JWT_EXPIRATION` | No | `3600000` | Access-token lifetime in milliseconds. |
| `DB_URL` | dev: no / prod: yes | dev: `jdbc:postgresql://localhost:5432/money_manager` | JDBC connection URL. |
| `DB_USERNAME` | dev: no / prod: yes | dev: `postgres` | Database user. |
| `DB_PASSWORD` | dev: no / prod: yes | dev: `postgres` | Database password. |
| `DDL_AUTO` | No | dev: `update`, prod: `validate` | Hibernate schema mode. |
| `H2_CONSOLE_ENABLED` | No | dev: `true`, prod: `false` | H2 console (dev only). |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:5173` | Comma-separated list of allowed browser origins. |
| `DB_NAME` | No | `money_manager` | Only used by Docker Compose. |

The `prod` profile intentionally has no defaults for `DB_URL`, `DB_USERNAME`, or
`DB_PASSWORD`: the application will fail fast instead of silently using development
credentials.

## Windows System Environment Variables

1. Generate a secret with WSL/Git Bash: `openssl rand -hex 32`
   (or reuse the value already written to `backend/.env`).
2. Open **System Properties → Advanced → Environment Variables**
   (`Win + R`, then `sysdm.cpl`).
3. Under **User variables**, add:
   - `JWT_SECRET` — the generated value (required)
   - `DB_PASSWORD` — your local PostgreSQL password, if it is not `postgres`
   - Optionally: `SPRING_PROFILES_ACTIVE`, `DB_URL`, `DB_USERNAME`, `JWT_EXPIRATION`,
     `CORS_ALLOWED_ORIGINS`
4. **Restart IntelliJ IDEA and any open terminals.** Environment variables are read when
   the process starts, so already-running processes will not see them.
5. Verify in `cmd`: `echo %JWT_SECRET%`

IntelliJ alternative: **Run → Edit Configurations → Environment variables**. This is
usually easier than system-wide variables and does not require restarting Windows.

## Local run

```bash
# 1. Start PostgreSQL (from backend/)
docker compose up -d postgres

# 2. Backend (from backend/)
export JWT_SECRET="<your-secret>"
./mvnw spring-boot:run

# 3. Frontend (from frontend/)
npm install
npm run dev
```

On Windows, set `JWT_SECRET` in IntelliJ or via `set JWT_SECRET=...` in `cmd` instead of
`export`.

## Docker Compose

`backend/.env` is read automatically by `docker compose` when the command is run from
`backend/`. Copy `backend/.env.example` to `backend/.env` and fill in the values. Compose
refuses to start the backend if `JWT_SECRET` is missing.

## Rotating the JWT secret

The original secret was committed to git history, so it is considered leaked. It has been
replaced with a new value. Tokens signed with the old secret are now invalid and users
must sign in again. To rotate again, generate a new value, update the environment
variable, and restart the backend.

## Frontend

The frontend currently has no environment variables. Its API base URL is hardcoded to
`http://localhost:8080`; moving it to `VITE_API_BASE_URL` is a later step.
