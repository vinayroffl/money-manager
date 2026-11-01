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

No local Maven install is required: `./mvnw` downloads the pinned Maven version on first
use. On Windows, use `mvnw.cmd` (or `.\mvnw.cmd` in PowerShell).

The frontend defaults to `http://localhost:8080` in development. If the backend runs
elsewhere, copy `frontend/.env.example` to `frontend/.env` and set
`VITE_API_BASE_URL` (see [Frontend](#frontend)).

## Docker Compose

`backend/.env` is read automatically by `docker compose` when the command is run from
`backend/`. Copy `backend/.env.example` to `backend/.env` and fill in the values. Compose
refuses to start the backend if `JWT_SECRET` is missing.

`docker compose up -d --build backend` builds the image from source using the Maven
Wrapper inside the build stage, so no local Maven install or prebuilt jar is required.

## Database migrations

The schema is managed by Flyway. Migrations live in
`backend/src/main/resources/db/migration`.

- `V1__initial_schema.sql` is an exact snapshot of the pre-Flyway schema, including the
  Hibernate-generated constraint names.
- `V2__normalize_constraints_and_add_indexes.sql` renames those constraints and adds
  indexes for the transaction and budget queries.
- `dev` uses `baseline-on-migrate: true` with `baseline-version: 1`. An existing
  Hibernate-created database is baselined at V1 and V2 runs on top; an empty database
  runs V1 and V2 from scratch.
- `prod` never baselines automatically. A non-empty database without a Flyway history
  table fails on purpose.
- Hibernate now runs with `ddl-auto: validate` in `dev` and `prod`. It no longer changes
  the schema.
- Tests use H2 with `create-drop`; Flyway is disabled in the `test` profile.

To change the schema, add the next numbered migration (for example
`V3__add_accounts.sql`). Never edit a migration that has already been applied.

## Rotating the JWT secret

The original secret was committed to git history, so it is considered leaked. It has been
replaced with a new value. Tokens signed with the old secret are now invalid and users
must sign in again. To rotate again, generate a new value, update the environment
variable, and restart the backend.

## Frontend

| Variable | Required | Default | Description |
| --- | --- | --- | --- |
| `VITE_API_BASE_URL` | No | dev: `http://localhost:8080`, prod: same origin | Base URL of the backend API. A trailing slash is ignored. |

Create `frontend/.env` from `frontend/.env.example`:

```bash
cd frontend
cp .env.example .env
```

Vite only exposes variables prefixed with `VITE_` to browser code, so **never put
secrets in `VITE_*` variables**. They are embedded in the client bundle.

For a production build, set `VITE_API_BASE_URL` to the public backend URL (for example
`https://api.example.com`) before running `npm run build`. If it is unset, the build
uses the same origin as the frontend.
