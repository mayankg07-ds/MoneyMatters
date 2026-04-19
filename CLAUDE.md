# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**MoneyMatters** is a full-stack Indian personal finance platform:
- **Backend**: Spring Boot 3.2 (Java 17), port `8082`, context path `/api`
- **Frontend**: React 19 + Vite, port `5173`
- **Database**: H2 in-memory (dev, data lost on restart) / Railway PostgreSQL (prod) — credentials via env vars
- **Auth**: Clerk (frontend) + Spring Security OAuth2 Resource Server (backend JWT validation)

## Commands

### Backend
```bash
# Run (from repo root) — load env vars first if using Railway DB
set -a && source .env && set +a
mvn spring-boot:run

# Build (skip tests)
mvn clean package -DskipTests

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=SIPCalculatorServiceTest

# Run a specific test method
mvn test -Dtest=SIPCalculatorServiceTest#testBasicSIPCalculation
```

> Note: `mvnw` wrapper is in `.gitignore` and not present in the repo. Use system `mvn` instead.

### Frontend
```bash
cd frontend

# Dev server
npm run dev

# Build
npm run build

# Lint
npm run lint
```

### Dev URLs
- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8082/api`
- Swagger UI: `http://localhost:8082/api/swagger-ui/index.html`
- H2 Console: `http://localhost:8082/api/h2-console` (JDBC URL: `jdbc:h2:mem:moneymatters-db`, user: `sa`, no password)
- Health: `http://localhost:8082/api/actuator/health`

## Architecture

### Backend Package Structure (`com.moneymatters`)

```
common/
  config/       SecurityConfig (JWT auth), WebConfig (CORS — origins from env), CacheConfig
  dto/          ApiResponse<T> — uniform {success, data, message, timestamp} wrapper
  exception/    GlobalExceptionHandler (@RestControllerAdvice)

user/           User entity + UserRepository + UserService (auto-creates user on first JWT request)

calculators/    Stateless POST endpoints — no DB, pure math
  service/      FinancialMathService (FV/PV/EMI primitives), 6 calculator Impl classes
  controller/   6 controllers under /api/v1/calculators/
  dto/          Request/Response DTOs + ChartPoint (used by all calculators for chart data)
  util/         CalculationUtils (BigDecimal helpers, always use these for financial math)

portfolio/      Stateful CRUD module
  entity/       Holding, Transaction (JPA with composite indexes)
  repository/   HoldingRepository, TransactionRepository
  service/      HoldingServiceImpl, TransactionServiceImpl,
                PortfolioAnalyticsServiceImpl (@Cacheable), StockPriceService (@Cacheable),
                PriceUpdateService (@Scheduled)
  controller/   HoldingController, TransactionController,
                PortfolioAnalyticsController, StockPriceController
  util/         XIRRCalculator (Newton-Raphson solver)
```

### Calculator Endpoints (all `POST /api/v1/calculators/...`)
| Calculator | Path |
|---|---|
| SIP Step-up | `/sip-stepup/calculate` |
| Retirement Planner | `/retirement/plan` |
| Loan Analyzer | `/loan/analyze` |
| Loan Comparison | `/loan/compare` |
| Asset Allocation | `/asset-allocation/rebalance` |
| Cashflow Planner | `/cashflow/project` |
| SWP | `/swp/calculate` |

FD, RD, PPF calculators are **frontend-only** (no backend endpoints).

### Portfolio Endpoints (all under `/api/v1/portfolio/`)
All portfolio endpoints require a valid Clerk JWT Bearer token. `userId` is extracted from `jwt.getSubject()` — never passed in the URL or body.

- Holdings: `/holdings` (CRUD), `/holdings/user` (list), `/holdings/user/summary`, `/holdings/user/refresh-prices`
- Transactions: `/transactions` (CRUD), `/transactions/user`, `/transactions/user/symbol/{symbol}` (supports BUY, SELL, DIVIDEND, BONUS, SPLIT)
- Analytics: `/analytics/user` (XIRR, CAGR, gainers/losers — cached per userId), `/analytics/user/date-range`
- Stock Prices: `/prices/current/{symbol}`, `/prices/details/{symbol}`, `/prices/update/user`

### Frontend Structure (`frontend/src/`)
- `App.jsx` — router root; `ProtectedRoute` uses Clerk's `useAuth()`; calls `useAxiosInterceptor()` in `AppLayout` to attach JWT to every request
- `services/setupAxiosInterceptor.js` — React hook that attaches Clerk `getToken()` as `Authorization: Bearer <token>` on all Axios requests
- `services/api.js` — centralized Axios service layer; all portfolio API calls are here; no `userId` params (identity comes from JWT)
- `pages/` — one file per route; calculator pages under `pages/calculators/`
- `components/` — shared UI: `Sidebar`, `Toast`, `GradientText`; landing page sections under `components/landing/`
- `pages/Register.jsx` — uses Clerk's `<SignUp />` component; no custom form or localStorage

### Key Cross-Cutting Patterns

**API Response**: Every backend endpoint returns `ApiResponse<T>` with `{success, data, message, timestamp}`.

**Financial math**: Always use `CalculationUtils` and `FinancialMathService` for monetary calculations. Never use `float`/`double` — all financial values use `BigDecimal` with `RoundingMode.HALF_UP`.

**Caching**: Two Spring caches exist:
- `portfolioAnalytics` — keyed by `userId`, evicted via `clearAnalyticsCache(userId)` on any holding/transaction mutation
- `stockPrices` — keyed by symbol; auto-refreshed every 15 min on weekdays 9:15–15:30 IST via `PriceUpdateService`

**Stock symbols**: Yahoo Finance format — NSE uses `SYMBOL.NS`, BSE uses `CODE.BO`. The service auto-appends the suffix based on the `exchange` field. See `INDIAN_STOCK_SYMBOLS.md` for reference.

**Transaction side effects**: Recording a transaction mutates the holding (BUY → weighted avg price; SELL → FIFO cost basis; BONUS/SPLIT → qty adjustment). Transaction deletion does **not** reverse these effects (known stub).

### Authentication Pattern

**Backend identity**: All controllers use `@AuthenticationPrincipal Jwt jwt` and extract the user via `jwt.getSubject()`. Never pass `userId` in URLs, request params, or body.

**`clerkUserId` field**: `Holding` and `Transaction` entities store `clerkUserId` (Java field) mapped to `user_id` DB column via `@Column(name = "user_id")`. Repository methods are named `*ByClerkUserId*`.

**UserService**: Call `userService.ensureUserExists(clerkUserId, email)` in mutation endpoints to auto-provision the `users` table row on first request.

**Tests**: Integration tests use `SecurityMockMvcRequestPostProcessors.jwt()` from `spring-security-test` to inject a mock JWT — no real JWKS endpoint needed. Unit service tests pass a `TEST_USER` string constant directly to service methods.

**Environment variables** (see `.env.example`):
- `CLERK_JWK_SET_URI` — Clerk JWKS endpoint (default: `https://classic-quail-60.clerk.accounts.dev/.well-known/jwks.json`)
- `CLERK_ISSUER_URI` — Clerk issuer (default: `https://classic-quail-60.clerk.accounts.dev`)
- `CORS_ALLOWED_ORIGINS` — comma-separated allowed origins
- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` — PostgreSQL (prod)
- `VITE_CLERK_PUBLISHABLE_KEY` — frontend env var for Clerk

## Known Issues / Gaps

- **Transaction reversal is stubbed** — deleting a transaction does not reverse its effect on the holding
- **Date-range analytics is broken** — `getPortfolioAnalyticsForDateRange` ignores date params and returns full analytics
- **Dashboard portfolio-growth chart uses simulated data** — not real historical values
- **Settings page is unimplemented** — route exists, page is empty
- **No Flyway/Liquibase** — schema managed by `ddl-auto: update`; data resets on H2 restart
- **H2 Console only in dev** — H2 console is disabled when connecting to Railway PostgreSQL
