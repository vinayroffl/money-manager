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
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:5173` | Comma-separated list of allowed browser origins. |

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
# 1. Start PostgreSQL (from the repository root; create .env first as described in
#    "Full-stack Docker Compose")
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

### Health check

The backend exposes `/actuator/health` without authentication. It returns HTTP 200 with
`{"status":"UP"}` when the application and database are reachable:

```bash
curl http://localhost:8080/actuator/health
```

## Full-stack Docker Compose

The root `docker-compose.yml` runs PostgreSQL, the backend, and the frontend:

- `postgres` — PostgreSQL 15 with data in the `money-manager_postgres_data` volume.
- `backend` — the Spring Boot image using the `prod` profile. Flyway runs on startup.
- `frontend` — Nginx serving the Vite build and proxying `/api` to the backend.

### 1. Configure environment variables

From the repository root:

```bash
cp .env.example .env
openssl rand -hex 32
```

Put the generated value in `.env` as `JWT_SECRET`. The defaults work for a local
machine; adjust these if needed:

| Variable | Default | Purpose |
| --- | --- | --- |
| `POSTGRES_DB` | `money_manager` | Database name. |
| `POSTGRES_USER` | `postgres` | Database user. |
| `POSTGRES_PASSWORD` | `postgres` | Database password. |
| `POSTGRES_PORT` | `5432` | Host port for PostgreSQL. |
| `JWT_SECRET` | — | Required HS256 signing key. |
| `BACKEND_PORT` | `8080` | Host port for the backend. |
| `FRONTEND_PORT` | `3000` | Host port for the frontend. |
| `VITE_API_BASE_URL` | empty | Empty uses the same-origin Nginx proxy. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Only needed for direct backend calls. |

`.env` is gitignored. `backend/.env` is still used by local Maven/IDE runs and is not read
by the root Compose file.

### 2. Build and start

```bash
docker compose up -d --build
docker compose ps
```

Then open:

- Frontend: <http://localhost:3000>
- API directly: <http://localhost:8080>
- Health: <http://localhost:8080/actuator/health>

The frontend calls `/api` on its own origin, and Nginx forwards those requests to
`backend:8080`. This avoids CORS and does not require `VITE_API_BASE_URL`.

### 3. Common commands

```bash
# Rebuild after code changes
docker compose up -d --build

# Follow logs
docker compose logs -f backend frontend

# Stop containers (database data is preserved)
docker compose down

# Stop and delete the database volume
docker compose down -v
```

On Windows, `backend\deploy.bat` runs the build and start steps.

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
- Hibernate runs with `ddl-auto: validate` in `dev`, `prod`, and `test`. It never changes
  the schema.
- Tests run the real Flyway migrations against a Testcontainers-managed PostgreSQL 15
  database and then validate the entities against the resulting schema. Docker must be
  running when you execute `./mvnw verify`.

To change the schema, add the next numbered migration (for example
`V3__add_accounts.sql`). Never edit a migration that has already been applied.

## Test coverage

`./mvnw verify` runs JaCoCo and fails if overall line coverage drops below 90% or branch
coverage below 70%. The HTML report is written to
`backend/target/site/jacoco/index.html`.

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
