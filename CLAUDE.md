# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**MoneyMatters** is a full-stack Indian personal finance platform:
- **Backend**: Spring Boot 3.2 (Java 17), port `8082`, context path `/api`
- **Frontend**: React 19 + Vite, port `5173`
- **Database**: H2 in-memory (dev) — all data is lost on restart
- **Auth**: Clerk (frontend only — the backend has **no auth** and trusts `userId` from request params/body)

## Commands

### Backend
```bash
# Run (from repo root)
./mvnw spring-boot:run

# Build (skip tests)
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=SIPCalculatorServiceTest

# Run a specific test method
./mvnw test -Dtest=SIPCalculatorServiceTest#testBasicSIPCalculation
```

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
  config/       WebConfig (CORS for :5173/:3000), CacheConfig
  dto/          ApiResponse<T> — uniform {success, data, message, timestamp} wrapper
  exception/    GlobalExceptionHandler (@RestControllerAdvice)

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
- Holdings: `/holdings` (CRUD + `/user/{userId}/summary`, `/refresh-prices`)
- Transactions: `/transactions` (supports BUY, SELL, DIVIDEND, BONUS, SPLIT)
- Analytics: `/analytics/user/{userId}` (XIRR, CAGR, gainers/losers — cached per userId)
- Stock Prices: `/prices/current/{symbol}`, `/prices/details/{symbol}`

### Frontend Structure (`frontend/src/`)
- `App.jsx` — router root; `ProtectedRoute` uses Clerk's `useAuth()`; unauthenticated users redirected to `/login`
- `pages/` — one file per route; calculator pages under `pages/calculators/`
- `components/` — shared UI: `Sidebar`, `Toast`, `GradientText`; landing page sections under `components/landing/`
- All API calls go through Axios; no centralized `api.js` service layer — calls are made directly in page components

### Key Cross-Cutting Patterns

**API Response**: Every backend endpoint returns `ApiResponse<T>` with `{success, data, message, timestamp}`.

**Financial math**: Always use `CalculationUtils` and `FinancialMathService` for monetary calculations. Never use `float`/`double` — all financial values use `BigDecimal` with `RoundingMode.HALF_UP`.

**Caching**: Two Spring caches exist:
- `portfolioAnalytics` — keyed by `userId`, evicted via `clearAnalyticsCache(userId)` on any holding/transaction mutation
- `stockPrices` — keyed by symbol; auto-refreshed every 15 min on weekdays 9:15–15:30 IST via `PriceUpdateService`

**Stock symbols**: Yahoo Finance format — NSE uses `SYMBOL.NS`, BSE uses `CODE.BO`. The service auto-appends the suffix based on the `exchange` field. See `INDIAN_STOCK_SYMBOLS.md` for reference.

**Transaction side effects**: Recording a transaction mutates the holding (BUY → weighted avg price; SELL → FIFO cost basis; BONUS/SPLIT → qty adjustment). Transaction deletion does **not** reverse these effects (known stub).

## Known Issues / Gaps

- **No backend authentication** — `userId` is trusted from the request; any caller can access any user's data
- **Transaction reversal is stubbed** — deleting a transaction does not reverse its effect on the holding
- **Date-range analytics is broken** — `getPortfolioAnalyticsForDateRange` ignores date params and returns full analytics
- **Dashboard portfolio-growth chart uses simulated data** — not real historical values
- **Settings page is unimplemented** — route exists, page is empty
- **H2 only** — no Flyway/Liquibase, no PostgreSQL config, data resets on every restart
