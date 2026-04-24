# MoneyMatters — Complete Codebase Summary (as of 2026-04-24)

> This document is maintained as the authoritative, detailed handoff snapshot of the repository. It supersedes earlier revisions by covering every change since the **2026-04-04** snapshot (commit `020dedd`), including the full Clerk authentication migration, the portfolio autocomplete + UX hardening, the NVIDIA Nemotron AI advisor, the PowerShell DX helper, and the recruiter-facing README rewrite.

---

## 0. What changed since the last summary (timeline)

The previous summary froze the codebase at commit `020dedd` (2026-04-04). Since then the repository has received five major feature waves and one large infrastructure pass. In commit order (newest first):

| Commit   | Date       | Headline change                                                                                          |
| -------- | ---------- | -------------------------------------------------------------------------------------------------------- |
| `ebc6d35` | 2026-04-21 | Merge branch `master` into `main` — conflict resolution across `application.yml` after the parallel PRs  |
| `8b4cbc8` | 2026-04-21 | **NVIDIA Nemotron AI advisor** — new `ai/` module, Explain-on-every-calculator, Portfolio Analyser card   |
| `eccf982` | 2026-04-20 | `CLAUDE.md` documentation update covering the Clerk auth feature                                          |
| `3998741` | 2026-04-19 | **Clerk JWT authentication on the backend** — Spring Security OAuth2 Resource Server + config hardening  |
| `50e7b87` | 2026-04-19 | Same feature committed earlier in the day (duplicate due to branching); kept as history                   |
| `cf5261b` | 2026-04-17 | **Clerk auth migration completion on the frontend** — `<SignIn />` / `<SignUp />`, JWT axios interceptor |
| `3cd9f6c` | 2026-04-17 | Project context guides (`MoneyMatters_Codebase_Analysis.md`, CLAUDE.md polish) + `main.jsx` Clerk wiring |
| `63e0719` | 2026-04-06 | **Clerk auth integration start** + portfolio stock autocomplete (NSE/BSE nifty500 lookup)                 |
| plus      | 2026-04-21 | `run-backend.ps1` (PowerShell `.env` loader), `SPRING_DATASOURCE_*` fallbacks in `application.yml`, full README rewrite with screenshots in `docs/screenshots/` |

Nothing from the earlier summary has been removed; everything below describes the current state, not a diff.

---

## 1. What MoneyMatters is

A **full-stack Indian personal finance platform** that combines three product pillars:

1. **10 financial calculators** (SIP step-up, Retirement Planner, Loan EMI + Prepayment, Loan Comparison, Asset Allocation Rebalancer, Cashflow Planner, SWP, FD, RD, PPF) — mix of stateless Spring endpoints and client-side computations, all with charted outputs.
2. **Live-priced stock portfolio tracker** — Yahoo Finance v8 chart API for prices and v10 `quoteSummary` for fundamentals, scheduled refresh on IST market hours, XIRR + CAGR analytics, full transaction ledger (BUY / SELL / DIVIDEND / BONUS / SPLIT), FIFO cost basis.
3. **AI advisor powered by NVIDIA Nemotron** — contextual explanations on every calculator result, plus a holistic portfolio analyser that consumes real fundamentals and returns a structured report with a health score, risks, and recommendations. Stateless follow-up channel.

The project is structured as a monorepo: Spring Boot 3.2 backend (`src/`) and a React 19 / Vite frontend (`frontend/`), deployed independently.

---

## 2. Technology Stack (current)

| Layer              | Technology                                                                              |
| ------------------ | ----------------------------------------------------------------------------------------- |
| Backend framework  | Spring Boot 3.2.0, Java 17, Spring MVC, Spring Data JPA, Spring Cache, Spring Scheduling  |
| **Auth**           | **Spring Security OAuth2 Resource Server (JWT) validating against Clerk JWKS**            |
| Database           | H2 in-memory (dev) / **Railway PostgreSQL (prod)** — credentials via env vars             |
| Financial math     | `BigDecimal` with `RoundingMode.HALF_UP`, Apache Commons Math 3.6.1, Newton-Raphson XIRR  |
| Market data        | Yahoo Finance **v8 chart API** (price) and **v10 quoteSummary API** (fundamentals)        |
| **AI**             | **NVIDIA NIM (Nemotron chat completions endpoint, OpenAI-compatible)**                    |
| HTTP client        | Java 17 built-in `java.net.http.HttpClient` (used by both Yahoo and Nemotron services)    |
| Caching            | `@Cacheable` + `ConcurrentMapCacheManager` (4 caches, see §6)                             |
| Rate limiting      | Custom in-memory sliding-window limiter (per-user, 1-hour window)                         |
| API docs           | Swagger / OpenAPI via `springdoc-openapi-starter-webmvc-ui` at `/api/swagger-ui/index.html`|
| Frontend framework | React 19, Vite 5, React Router 7, Chart.js 4, Axios                                       |
| **Frontend auth**  | **`@clerk/clerk-react` — `<SignIn />`, `<SignUp />`, `useAuth()`, `getToken()`**          |
| UI                 | Custom dark theme, Tailwind utility classes, Radix UI Dialog, Lucide React, Motion        |
| Build              | Maven (backend), npm / Vite (frontend)                                                    |
| DX                 | `run-backend.ps1` — loads `.env` into the PowerShell process env, then runs Maven         |

---

## 3. Top-Level Repository Layout

```
FinProject/
├── src/main/java/com/moneymatters/   Backend source (see §4)
│   ├── MoneyMattersApplication.java   @SpringBootApplication + @EnableCaching + @EnableScheduling
│   ├── ai/                            NVIDIA Nemotron AI advisor module  (NEW)
│   ├── calculators/                   Stateless calculator endpoints
│   ├── common/                        ApiResponse<T>, SecurityConfig, WebConfig, CacheConfig, GlobalExceptionHandler
│   ├── portfolio/                     Stateful holdings / transactions / analytics / prices
│   └── user/                          User entity + UserService (Clerk-provisioned)
├── src/main/resources/
│   └── application.yml                Spring config (security.oauth2, caches, datasource, nvidia, ai.rate-limit)
├── src/test/                          JUnit + Spring MockMvc tests (mock-JWT for secured endpoints)
├── frontend/                          React 19 + Vite app (see §7)
├── docs/
│   ├── PORTFOLIO_API_DOCUMENTATION.md
│   └── screenshots/                   heropage.png, dashboard.png, ai-feature.png (used by README)
├── logs/                              `logs/moneymatters.log` (rolling, 10MB × 30)
├── design/                            16 Figma-exported screen mockups
├── ind_nifty500list.csv               NSE 500 list (source for autocomplete JSON)
├── .env.example                       DB + Clerk + NVIDIA env-var template
├── run-backend.ps1                    (NEW) Loads .env into PowerShell, runs `mvn spring-boot:run`
├── pom.xml                            Maven config (includes spring-boot-starter-oauth2-resource-server, spring-security-test)
├── CLAUDE.md                          Contributor / AI-assistant guide for this repo
├── CodeBaseSummaryTillNow.md          Original snapshot (frozen at 2026-04-04)
├── LatestCodeBaseSummary.md           (this file) — up-to-date snapshot as of 2026-04-24
├── MoneyMatters_Codebase_Analysis.md  Older, analytical summary
├── README.md                          (REWRITTEN) Recruiter-facing project README with screenshots
├── INDIAN_STOCK_SYMBOLS.md            NSE/BSE → Yahoo symbol mapping reference
├── PERFORMANCE_OPTIMIZATIONS.md       Index / cache design notes
├── PORTFOLIO_API_DOCUMENTATION.md     50+ portfolio endpoints reference
└── WEEK1…WEEK5_LEARNINGS.md           Dev journals
```

---

## 4. Backend Architecture — `com.moneymatters`

```
com.moneymatters
├── MoneyMattersApplication          @SpringBootApplication, @EnableCaching, @EnableScheduling,
│                                    @EnableConfigurationProperties({NvidiaProperties, AiRateLimitProperties})
│
├── common/
│   ├── config/
│   │   ├── SecurityConfig          (NEW) OAuth2 Resource Server with Clerk JWKS; stateless sessions
│   │   ├── WebConfig                CORS — allowed origins from CORS_ALLOWED_ORIGINS env var
│   │   └── CacheConfig              ConcurrentMapCacheManager: portfolioAnalytics, stockPrices,
│   │                                stockFundamentals, aiPortfolioAnalysis
│   ├── dto/
│   │   └── ApiResponse<T>           Uniform { success, data, message, timestamp }
│   └── exception/
│       └── GlobalExceptionHandler   @RestControllerAdvice: validation, generic 500,
│                                    RateLimitExceededException → 429 + Retry-After (NEW)
│
├── user/                            (NEW in Clerk migration)
│   ├── User                         JPA entity keyed by clerkUserId (String PK)
│   ├── UserRepository               Spring Data JPA
│   └── UserService                  ensureUserExists(clerkUserId, email) — auto-provisions on first JWT
│
├── calculators/                     Unchanged structurally; 7 stateless endpoints
│   ├── service/
│   │   ├── FinancialMathService     FV, PV, EMI, Annuity FV, PVA, inflation adjustment (BigDecimal)
│   │   ├── SIPCalculatorServiceImpl
│   │   ├── RetirementPlannerServiceImpl
│   │   ├── LoanAnalyzerServiceImpl           (includes LoanComparison)
│   │   ├── AssetAllocationServiceImpl
│   │   ├── CashflowPlannerServiceImpl
│   │   └── SWPCalculatorServiceImpl
│   ├── dto/                          Request / Response pairs + ChartPoint
│   ├── util/
│   │   └── CalculationUtils          BigDecimal helpers (HALF_UP rounding)
│   └── controller/                   @PostMapping + @Valid
│
├── portfolio/                       Stateful; all endpoints now auth-protected
│   ├── entity/
│   │   ├── Holding                  clerkUserId (Java) ↔ user_id (DB column);
│   │   │                            composite index (clerkUserId, assetSymbol) + (assetType)
│   │   └── Transaction              composite index (clerkUserId, transactionDate),
│   │                                (holdingId), (transactionType)
│   ├── repository/
│   │   ├── HoldingRepository        findActiveHoldingsByClerkUserId, existsByClerkUserIdAndAssetSymbol
│   │   └── TransactionRepository    findBuyTransactionsForAsset (FIFO), date-range queries
│   ├── service/
│   │   ├── HoldingServiceImpl       CRUD + batch price refresh + cache eviction on mutation
│   │   ├── TransactionServiceImpl   Records txn + mutates holding (weighted avg / FIFO / bonus / split)
│   │   ├── PortfolioAnalyticsServiceImpl
│   │   │                            @Cacheable(value="portfolioAnalytics", key="#userId");
│   │   │                            XIRR + CAGR + gainers/losers + asset-wise P&L + dividends
│   │   ├── StockPriceService        @Cacheable(value="stockPrices", key="#symbol"); Yahoo v8 chart
│   │   └── PriceUpdateService       @Scheduled every 15 min Mon-Fri 9:15–15:30 IST
│   ├── util/
│   │   └── XIRRCalculator           Newton-Raphson over BigDecimal
│   └── controller/
│       ├── HoldingController           8 endpoints — identity comes from @AuthenticationPrincipal Jwt
│       ├── TransactionController       6 endpoints
│       ├── PortfolioAnalyticsController 2 endpoints
│       └── StockPriceController         4 endpoints
│
└── ai/                              (NEW — commit 8b4cbc8)
    ├── config/
    │   ├── NvidiaProperties         @ConfigurationProperties("nvidia"): apiKey, apiUrl, model,
    │   │                            maxTokens (1500 default), temperature (0.6 default)
    │   └── AiRateLimitProperties    @ConfigurationProperties("ai.rate-limit"): maxRequestsPerHour (10)
    ├── dto/
    │   ├── ExplainRequest           { type, inputs: Map, result: Map }
    │   ├── ExplainResponse          { explanation, remainingRequests }
    │   └── FollowupRequest          { topic, context, question } — stateless follow-up
    ├── exception/
    │   └── RateLimitExceededException  custom RuntimeException with limit + retryAfterSeconds
    ├── service/
    │   ├── NemotronService          HttpClient → NVIDIA NIM /v1/chat/completions;
    │   │                            catches every exception and returns a user-visible fallback
    │   │                            string — never propagates HTTP errors to the controller
    │   ├── MarketFundamentalsService @Cacheable("stockFundamentals"); Yahoo v10 quoteSummary with
    │   │                            modules = price, defaultKeyStatistics, financialData,
    │   │                            summaryDetail, assetProfile; returns PE, ROE, 50/200 DMA, beta,
    │   │                            revenue/earnings growth, analyst rating + target, dividend yield
    │   ├── AiRateLimiter            ConcurrentHashMap<userId, Deque<Instant>> sliding window
    │   ├── PromptBuilder            Owns every prompt: calculator system prompt, 10 per-type user
    │   │                            prompts, portfolio system prompt, portfolio user prompt (renders
    │   │                            the rich fundamentals card for each holding), follow-up system
    │   │                            prompts for CALCULATOR vs PORTFOLIO. Context truncated at 3000
    │   │                            chars.
    │   └── AiPortfolioAnalysisService
    │                                 @Cacheable(value="aiPortfolioAnalysis", key="#userId") —
    │                                 extracted into its own bean because Spring AOP does not
    │                                 intercept self-invocation. Null-soft on analytics failures
    │                                 (partial-failure fallback). 15-minute effective cache TTL
    │                                 by eviction on mutation.
    └── controller/
        └── AiController            @RequestMapping("/v1/ai") — 4 endpoints, all rate-limited:
                                     POST /explain-calculator, POST /analyse-portfolio,
                                     POST /followup, GET /quota
```

---

## 5. Authentication — Clerk + Spring Security OAuth2

This replaced the previous hard-coded-`userId` system entirely.

### Frontend side
- `@clerk/clerk-react` is wired in `frontend/src/main.jsx` with `VITE_CLERK_PUBLISHABLE_KEY`.
- **`Login.jsx` renders `<SignIn />`** — no custom form, no localStorage.
- **`Register.jsx` renders `<SignUp />`** — same.
- **`App.jsx`** wraps all app routes in `<SignedIn>` / `<SignedOut>` guards; `ProtectedRoute` uses Clerk's `useAuth()`.
- **`services/setupAxiosInterceptor.js`** — a React hook that calls `getToken()` from Clerk and attaches `Authorization: Bearer <jwt>` to every outbound Axios request. Invoked once from `AppLayout` so it runs on every authenticated page.

### Backend side
- **`SecurityConfig`** (`common/config/SecurityConfig.java`) configures `oauth2ResourceServer().jwt()` with the Clerk JWKS URI (`CLERK_JWK_SET_URI`) and issuer URI (`CLERK_ISSUER_URI`). Sessions are stateless; all routes except `/actuator/**`, `/v3/api-docs/**`, and `/swagger-ui/**` require a valid JWT.
- **Identity in controllers**: every controller injects `@AuthenticationPrincipal Jwt jwt` and reads `jwt.getSubject()` (Clerk user id). No `userId` path param anywhere.
- **`UserService.ensureUserExists(clerkUserId, email)`** is called on mutation endpoints to auto-provision a row in the `users` table.
- **Entity mapping**: `Holding` and `Transaction` store the field `clerkUserId` (Java) mapped to DB column `user_id` via `@Column(name = "user_id")`. Repository methods are named `*ByClerkUserId*` (e.g. `findActiveHoldingsByClerkUserId`).
- **Env vars**:
  ```
  CLERK_JWK_SET_URI=https://<your-clerk-domain>/.well-known/jwks.json
  CLERK_ISSUER_URI=https://<your-clerk-domain>
  CORS_ALLOWED_ORIGINS=http://localhost:5173
  ```
- **Tests**: integration tests use `SecurityMockMvcRequestPostProcessors.jwt()` from `spring-security-test` to inject a mock JWT — no real JWKS network hop required. Unit service tests pass a `TEST_USER` string constant.

---

## 6. AI Advisor — NVIDIA Nemotron Integration (ai/ module)

### What it does
1. **Explain a calculator result** — every calculator's result screen has an *Explain this result with AI* button. It POSTs `{ type, inputs, result }` to `/v1/ai/explain-calculator`. The backend builds a system + user prompt specific to that calculator type and sends it to Nemotron. The response includes the explanation and four scoped suggested follow-ups (rendered as pills in the UI).
2. **Analyse my portfolio** — the Portfolio page shows a collapsible "AI Portfolio Analysis" card. Clicking *Analyse my portfolio* POSTs to `/v1/ai/analyse-portfolio`. The backend pulls the user's active holdings, enriches each with live Yahoo fundamentals (PE, ROE, 50/200 DMA, beta, analyst rating, analyst target, dividend yield, revenue/earnings growth), fetches the user's cached portfolio analytics (XIRR, CAGR, realized/unrealized split, sector allocation), and asks Nemotron for a structured report (health score, performers, risks, sector concentration, return context vs Nifty50, 3 actionable recommendations).
3. **Follow-up** — after either of the above, the user can pick a suggested question or type a custom one. The frontend calls `/v1/ai/followup` with `{ topic, context, question }` — `topic` is `CALCULATOR` or `PORTFOLIO`, `context` is the prior analysis text (truncated), `question` is the user's query. Stateless; no conversation thread is persisted server-side.

### Endpoints
| Method | Path                             | Purpose                                   | Auth | Rate-limited |
| ------ | -------------------------------- | ----------------------------------------- | ---- | ------------ |
| POST   | `/api/v1/ai/explain-calculator`  | Calculator explainer + follow-up pills    | ✅    | ✅            |
| POST   | `/api/v1/ai/analyse-portfolio`   | Full portfolio analysis with fundamentals | ✅    | ✅            |
| POST   | `/api/v1/ai/followup`            | Stateless follow-up Q/A                   | ✅    | ✅            |
| GET    | `/api/v1/ai/quota`               | Remaining requests in current 1-hour bucket | ✅  | ❌            |

### Key design decisions
- **Fail-soft over fail-fast for NVIDIA.** `NemotronService.chat()` catches every exception (HTTP 4xx/5xx, timeouts, parse errors, missing API key) and returns a plain-string fallback ("AI analysis temporarily unavailable…"). The controller renders whatever comes back — it never propagates a 500 to the client.
- **Per-symbol partial failure for fundamentals.** `AiPortfolioAnalysisService.analyse()` catches per-symbol exceptions from `MarketFundamentalsService.getFundamentals(yahooSymbol)` and substitutes an empty `Map.of()` so a single stale or delisted ticker doesn't block the whole report.
- **Null-safe analytics.** If `PortfolioAnalyticsServiceImpl.getPortfolioAnalytics()` throws, the prompt is still built with a "(Analytics unavailable)" marker so the LLM can still produce output from holdings alone.
- **Cache the LLM response, not the prompt.** `@Cacheable("aiPortfolioAnalysis", key="#userId")` on `analyse()` prevents burning rate-limited requests when the user re-opens the Portfolio page. The cache is evicted on any holding or transaction mutation (same eviction path used by `portfolioAnalytics`).
- **Self-invocation trap avoided.** The cached method lives on a separate `@Service` (`AiPortfolioAnalysisService`) so Spring's AOP proxy actually intercepts the call — calling a `@Cacheable` method from another method in the same bean bypasses the proxy entirely.
- **Rate limiter at service boundary.** `AiRateLimiter.checkAndConsume(userId)` uses a `ConcurrentHashMap<userId, Deque<Instant>>` and prunes entries older than 1 hour on each call. Exceeding the 10/hr budget throws `RateLimitExceededException`, translated to HTTP `429` with a `Retry-After` header by `GlobalExceptionHandler`.
- **Prompts centralised.** All calculator / portfolio / follow-up templates live in `PromptBuilder`. The portfolio user prompt renders a per-holding card with company name, sector, quantity, avg buy, current price, 52-week H/L, 50/200 DMA, PE, P/B, ROE, D/E, revenue growth, earnings growth, beta, analyst rating, analyst target price, and dividend yield.
- **Context truncation.** Follow-up context is truncated to 3000 characters before being sent to the LLM to stay inside the token budget.

### Env vars
```
NVIDIA_API_KEY=nvapi-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
NVIDIA_API_URL=https://integrate.api.nvidia.com/v1/chat/completions   # default
NVIDIA_MODEL=nvidia/nemotron-3-super-120b-a12b                        # default
NVIDIA_MAX_TOKENS=1500                                                # default
NVIDIA_TEMPERATURE=0.6                                                # default
AI_MAX_REQUESTS_PER_HOUR=10                                           # default
```

### Frontend touchpoints
- `frontend/src/components/ai/ExplainButton.jsx` — shared across all 10 calculator pages. Shows a gradient button, renders the LLM response, renders 4 suggested follow-up pills per calculator type, lets the user type a custom question, surfaces remaining quota, maps 429 to a readable "hit the AI limit" message.
- `frontend/src/components/ai/PortfolioAnalyser.jsx` — collapsible card on `Portfolio.jsx`. Same follow-up UX; suggested questions are portfolio-flavoured ("Is my portfolio too concentrated in one sector?", "Am I beating Nifty50 returns?", etc.).
- `frontend/src/services/api.js` → `aiApi = { explainCalculator, analysePortfolio, followup, quota }`.
- `ExplainButton` is wired into all 10 calculator pages (SIP, Retirement, Loan Analyzer, Asset Allocation, Cashflow, SWP, FD, RD, PPF, and Loan Comparison).

---

## 7. Frontend Application (React 19 / Vite)

### Routing — `App.jsx`
All app routes are guarded by Clerk (`<SignedIn>` / `<SignedOut>`) and wrapped in `AppLayout`, which calls `useAxiosInterceptor()` once so every Axios request carries the Clerk JWT.

| Route                          | Page                  | Status                                          |
| ------------------------------ | --------------------- | ----------------------------------------------- |
| `/`, `/login`                  | Login.jsx             | Clerk `<SignIn />`                              |
| `/register`                    | Register.jsx          | Clerk `<SignUp />`                              |
| `/dashboard`                   | Dashboard.jsx         | KPI cards, performance chart, top-5 holdings   |
| `/portfolio`                   | Portfolio.jsx         | Holdings CRUD + stock autocomplete + **AI Portfolio Analyser card** |
| `/analytics`                   | Analytics.jsx         | XIRR/CAGR, doughnut allocation, asset-wise P&L, CSV |
| `/transactions`                | Transactions.jsx      | Sortable/filterable ledger with CSV export     |
| `/calculators`                 | CalculatorsHome       | Grid of 10 calculator tiles                    |
| `/calculators/sip`             | SIPCalculator         | + `<ExplainButton />`                          |
| `/calculators/retirement`      | RetirementPlanner     | + `<ExplainButton />`                          |
| `/calculators/loan`            | LoanCalculator        | + `<ExplainButton />`                          |
| `/calculators/asset-allocation`| AssetAllocation       | + `<ExplainButton />`                          |
| `/calculators/cashflow`        | CashflowPlanner       | + `<ExplainButton />`                          |
| `/calculators/swp`             | SWPCalculator         | + `<ExplainButton />`                          |
| `/calculators/fd`              | FDCalculator          | Frontend-only math + `<ExplainButton />`       |
| `/calculators/rd`              | RDCalculator          | Frontend-only math + `<ExplainButton />`       |
| `/calculators/ppf`             | PPFCalculator         | Frontend-only math + `<ExplainButton />`       |
| `/settings`                    | Settings.jsx          | Routed but empty (known gap)                   |

### Services layer (`frontend/src/services/`)
- `api.js` — centralised Axios instance + grouped APIs:
  - `holdingApi` — holdings CRUD, summary, refresh-prices
  - `transactionApi` — transactions CRUD + date-range + FIFO
  - `analyticsApi` — portfolio analytics
  - `stockPriceApi` — price + details
  - `aiApi` — `explainCalculator`, `analysePortfolio`, `followup`, `quota` **(NEW)**
  - None of these take `userId` — identity comes from JWT.
- `setupAxiosInterceptor.js` — React hook that attaches Clerk `getToken()` as Bearer on every request.

### Portfolio autocomplete (commit 63e0719)
`Portfolio.jsx` uses `src/assets/nifty500.json` (3502 rows, built from `ind_nifty500list.csv`) to provide typeahead completion when adding a stock holding. Selecting a row fills in the asset symbol + exchange + name so users don't have to know the exact Yahoo-compatible ticker.

### UI / component inventory
- `Header.jsx`, `Sidebar.jsx`, `Toast.jsx` + `useToast()`, `GradientText.jsx` (animated app logo)
- `components/landing/LandingNavbar.jsx`, plus 16 Figma-exported mocks under `design/`
- **`components/ai/ExplainButton.jsx`** and **`components/ai/PortfolioAnalyser.jsx`** (NEW)
- Theme: custom dark CSS variables in `index.css` (with a `.spin` keyframe added for the AI loading icon)

---

## 8. Backend Endpoints — Full Current Inventory

All responses are wrapped in `ApiResponse<T>` = `{ success, data, message, timestamp }`.

### Calculators (`/api/v1/calculators/...`, stateless POSTs)
| Path                                    | Output                                                                      |
| --------------------------------------- | --------------------------------------------------------------------------- |
| `POST /sip-stepup/calculate`            | totalInvested, maturityValue, wealthGained, yearlyBreakdown, maturityCurve  |
| `POST /retirement/plan`                 | requiredCorpus, corpusShortfall, recommendedMonthlySIP, pre/post projections|
| `POST /loan/analyze`                    | EMI, amortization schedule, prepayment impact (REDUCE_TENURE / REDUCE_EMI)  |
| `POST /loan/compare`                    | Multi-option comparison + lowest-total-interest winner                      |
| `POST /asset-allocation/rebalance`      | Per-asset drift, BUY/SELL/HOLD amounts, balance flag                        |
| `POST /cashflow/project`                | Year-by-year projection, savings rate, cumulative savings                   |
| `POST /swp/calculate`                   | Monthly corpus depletion + sustainability flag + safe-withdrawal rate       |

FD / RD / PPF are frontend-only.

### Portfolio (`/api/v1/portfolio/...`, JWT required)
- **Holdings** (`/holdings`): `POST`, `PUT /{id}`, `DELETE /{id}`, `GET /{id}`, `GET /user`, `GET /user/summary`, `POST /{id}/refresh-price`, `POST /user/refresh-prices`
- **Transactions** (`/transactions`): `POST`, `GET /user`, `GET /user/symbol/{symbol}`, `GET /user/date-range`, `GET /user/symbol/{symbol}/fifo`, `DELETE /{id}`
- **Analytics** (`/analytics`): `GET /user`, `GET /user/date-range` (date-range still returns full analytics — known gap)
- **Prices** (`/prices`): `GET /current/{symbol}`, `GET /details/{symbol}`, `POST /update/holding/{id}`, `POST /update/user`

### AI (`/api/v1/ai/...`, JWT required, rate-limited 10/hr per user)
- `POST /explain-calculator`, `POST /analyse-portfolio`, `POST /followup`, `GET /quota`

### Ops
- `/api/actuator/**`, `/api/swagger-ui/index.html`, `/api/v3/api-docs` — all public

---

## 9. Caching, Scheduling, Rate-Limiting

### Spring caches (`CacheConfig`, `ConcurrentMapCacheManager`)
| Cache                | Key                 | Used by                                             | Eviction                                     |
| -------------------- | ------------------- | --------------------------------------------------- | -------------------------------------------- |
| `portfolioAnalytics` | `userId`            | `PortfolioAnalyticsServiceImpl.getPortfolioAnalytics`| `clearAnalyticsCache(userId)` on any mutation|
| `stockPrices`        | `symbol`            | `StockPriceService`                                 | On `@Scheduled` refresh                      |
| `stockFundamentals`  | `yahooSymbol`       | `MarketFundamentalsService` (NEW)                   | Never auto — relies on restart              |
| `aiPortfolioAnalysis`| `userId`            | `AiPortfolioAnalysisService.analyse` (NEW)          | `clearAnalyticsCache(userId)` on any mutation|

### Scheduled jobs
- `PriceUpdateService.refreshAllPrices()` — `@Scheduled(cron = "0 */15 9-15 * * MON-FRI", zone = "Asia/Kolkata")`. Batch-refreshes every active holding's price during IST market hours.

### Rate limiting
- `AiRateLimiter`: per-user 1-hour sliding window, 10 req/hr (configurable). Pruned lazily on each call. No Redis — in-memory only.
- Exceeding → `RateLimitExceededException` → HTTP 429 + `Retry-After` (sec) header.

---

## 10. Configuration & Environment (current)

### `src/main/resources/application.yml` (key blocks)
```yaml
spring:
  security.oauth2.resourceserver.jwt:
    jwk-set-uri: ${CLERK_JWK_SET_URI:https://classic-quail-60.clerk.accounts.dev/.well-known/jwks.json}
    issuer-uri: ${CLERK_ISSUER_URI:https://classic-quail-60.clerk.accounts.dev}

  cache:
    type: simple
    cache-names: [stockPrices, portfolioAnalytics, stockFundamentals, aiPortfolioAnalysis]

  datasource:
    url: ${SPRING_DATASOURCE_URL:${DATABASE_URL:jdbc:postgresql://localhost:5432/moneymatters}}
    username: ${SPRING_DATASOURCE_USERNAME:${DATABASE_USERNAME:postgres}}
    password: ${SPRING_DATASOURCE_PASSWORD:${DATABASE_PASSWORD:}}

  jpa:
    hibernate.ddl-auto: update
    properties.hibernate: { batch_size: 10, order_inserts: true, order_updates: true }

app.cors.allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000}

nvidia:
  api-key: ${NVIDIA_API_KEY:}
  api-url: ${NVIDIA_API_URL:https://integrate.api.nvidia.com/v1/chat/completions}
  model: ${NVIDIA_MODEL:nvidia/nemotron-3-super-120b-a12b}
  max-tokens: ${NVIDIA_MAX_TOKENS:1500}
  temperature: ${NVIDIA_TEMPERATURE:0.6}

ai.rate-limit.max-requests-per-hour: ${AI_MAX_REQUESTS_PER_HOUR:10}

server.port: 8082
server.servlet.context-path: /api
```

### `.env` (gitignored; keys only)
```
SPRING_DATASOURCE_URL / SPRING_DATASOURCE_USERNAME / SPRING_DATASOURCE_PASSWORD
DATABASE_URL / DATABASE_USERNAME / DATABASE_PASSWORD    (fallback pair)
CLERK_JWK_SET_URI / CLERK_ISSUER_URI
CORS_ALLOWED_ORIGINS
NVIDIA_API_KEY / NVIDIA_API_URL / NVIDIA_MODEL / NVIDIA_MAX_TOKENS / NVIDIA_TEMPERATURE
AI_MAX_REQUESTS_PER_HOUR
```

### Frontend `.env`
```
VITE_CLERK_PUBLISHABLE_KEY=pk_test_xxxxxxxxxxxxx
```

### Running locally
- **macOS / Linux**: `set -a && source .env && set +a && mvn spring-boot:run`
- **Windows / PowerShell**: `.\run-backend.ps1` (loads every `KEY=VALUE` line from `.env` into the process env, then runs Maven — solves the issue where PowerShell does not source `.env` automatically, causing Hikari to fall back to `localhost:5432`).

---

## 11. Tests

| Layer                       | Test classes                                                                                                                                                   |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Calculator unit tests       | `SIPCalculatorServiceTest`, `RetirementPlannerServiceTest`, `LoanAnalyzerServiceTest`, `SWPCalculatorServiceTest`, `CashflowPlannerServiceTest`, `AssetAllocationServiceTest`, `FinancialMathServiceTest`, `CalculationUtilsTest` |
| Portfolio unit tests        | `HoldingServiceTest`, `TransactionServiceTest`, `StockPriceServiceTest`, `XIRRCalculatorTest` — use a `TEST_USER` String constant                             |
| Integration tests           | `PortfolioIntegrationTest`, `CalculatorsIntegrationTest` — use `SecurityMockMvcRequestPostProcessors.jwt()` from `spring-security-test` to inject a mock JWT  |
| Smoke                       | `AppTest`                                                                                                                                                       |

Run with `mvn test` (all), `mvn test -Dtest=SIPCalculatorServiceTest`, or `mvn test -Dtest=SIPCalculatorServiceTest#testBasicSIPCalculation`.

---

## 12. Documentation (current)

| File                               | Purpose                                                                              |
| ---------------------------------- | ------------------------------------------------------------------------------------ |
| `README.md`                        | **Rewritten** recruiter-facing README with ASCII architecture, module layouts, tech-stack table, engineering decisions, screenshots, condensed + detailed API reference, known gaps. Embeds `docs/screenshots/heropage.png`, `dashboard.png`, `ai-feature.png`. |
| `CLAUDE.md`                        | Contributor / AI-assistant guide — covers auth pattern, entity field naming (`clerkUserId`), cache/eviction pattern, env vars, known gaps |
| `CodeBaseSummaryTillNow.md`        | Original snapshot frozen at 2026-04-04                                              |
| `LatestCodeBaseSummary.md`         | **This file** — full codebase snapshot as of 2026-04-24                             |
| `MoneyMatters_Codebase_Analysis.md`| Older analytical summary, kept for history                                          |
| `PORTFOLIO_API_DOCUMENTATION.md`   | 50+ endpoint reference, 4 workflow guides, error codes                              |
| `PERFORMANCE_OPTIMIZATIONS.md`     | Index strategy, cache design, benchmark estimates                                   |
| `INDIAN_STOCK_SYMBOLS.md`          | NSE/BSE → Yahoo symbol mapping reference                                            |
| `WEEK1…WEEK5_LEARNINGS.md`         | Weekly dev journal from initial setup through all 6 calculators                      |

### Screenshots (`docs/screenshots/`)
- `heropage.png` — landing page hero
- `dashboard.png` — portfolio overview
- `ai-feature.png` — AI "Explain this result" card with follow-up pills on the Loan calculator

---

## 13. Known Gaps / Incomplete Items (still open)

1. **Transaction reversal is stubbed** — `reverseTransactionEffect` in `TransactionServiceImpl` still logs "not fully implemented". Deleting a transaction does not roll back the holding mutation.
2. **Date-range analytics** — `getPortfolioAnalyticsForDateRange` ignores the date params and returns full analytics.
3. **Dashboard growth chart** uses simulated month-over-month values, not real historical snapshots.
4. **Settings page** is routed (`/settings`) but has no implementation.
5. **No Flyway / Liquibase** — schema managed by `hibernate.ddl-auto: update`. H2 resets on restart in dev; Railway PostgreSQL persists in prod.
6. **`stockFundamentals` cache has no automatic eviction** — values are held until JVM restart. Acceptable for demo but worth a TTL wrapper before prod.
7. **AI rate limiter is in-memory and per-JVM** — fine for single-instance deploy; would need Redis (or similar) to survive a horizontally-scaled rollout.
8. **Dev H2 console disabled** — `spring.h2.console.enabled: false`. Disabled when Railway PostgreSQL became the primary datasource.

---

## 14. Engineering Decisions Worth Calling Out

- **Identity comes from the JWT, never the URL.** Controllers use `@AuthenticationPrincipal Jwt jwt` + `jwt.getSubject()`; `userId` is never a path param, query param, or body field.
- **`BigDecimal` all the way down.** Every monetary value uses `BigDecimal` with `RoundingMode.HALF_UP`. XIRR uses Newton-Raphson over `BigDecimal`. No `double` in financial paths.
- **Cached service extracted into its own bean.** `AiPortfolioAnalysisService` exists as a separate `@Service` purely so Spring's AOP proxy can intercept the `@Cacheable` call — calling a `@Cacheable` method from within the same bean bypasses the proxy silently.
- **Fail-soft over fail-fast for external calls.** `NemotronService` swallows every exception and returns a user-visible string. Yahoo fundamentals failures are caught per-symbol so one bad ticker doesn't break the whole analysis.
- **Rate limiting at the service boundary.** `AiRateLimiter.checkAndConsume(userId)` is called at the top of every AI controller method. The exception is translated to 429 with `Retry-After` by `GlobalExceptionHandler`. The frontend renders "hit the AI limit, try again in ~N min".
- **Prompts centralised.** All 10 calculator templates + the portfolio prompt + the two follow-up system prompts live in `PromptBuilder`. Context is truncated to 3000 chars before being sent to the LLM.
- **YAML env-var fallback chain.** `application.yml` reads `${SPRING_DATASOURCE_URL:${DATABASE_URL:jdbc:postgresql://localhost:5432/moneymatters}}` so both the Spring-native and the legacy variable names work.
- **PowerShell DX.** `run-backend.ps1` was added because `source .env` doesn't exist on Windows. Without it, Spring fell back to `localhost:5432` on first run.

---

## 15. Commit-by-Commit Summary of Recent Changes

| Commit    | Scope                                                                                                                                                          |
| --------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ebc6d35` | Merge `master` into `main` — resolved the `application.yml` conflict between the SPRING_DATASOURCE_* fallback chain (main) and the older DATABASE_* variant (master), keeping the fallback chain. |
| `8b4cbc8` | **Full AI module** — 12 new backend files under `ai/` + 2 new frontend components + `aiApi` in `api.js` + `run-backend.ps1` + README rewrite + 3 screenshots. 34 files changed, +1613 / −267. Issues resolved: Spring cache self-invocation bug, partial-failure UX, PowerShell `.env` loading, API-key hygiene. |
| `eccf982` | `CLAUDE.md` updated to document the Clerk auth feature end-to-end: JWT extraction, `ensureUserExists`, entity naming, test pattern, env vars.                   |
| `3998741` | **Clerk JWT on the backend** — `SecurityConfig`, env vars, `@AuthenticationPrincipal Jwt jwt` on every controller, `UserService.ensureUserExists`, `Holding`/`Transaction` renamed `userId → clerkUserId`, repositories renamed, tests migrated to `jwt()` post-processor. `pom.xml` adds `spring-boot-starter-oauth2-resource-server` and `spring-security-test`. |
| `50e7b87` | Same Clerk-auth body committed earlier in the day via a side branch; kept in history.                                                                          |
| `cf5261b` | **Clerk frontend migration complete** — `Login.jsx` and `Register.jsx` render `<SignIn />`/`<SignUp />`; `App.jsx` wraps in `<SignedIn>`; `setupAxiosInterceptor.js` attaches Clerk JWT; `Sidebar.jsx` reads user from Clerk; `main.jsx` bootstraps `<ClerkProvider>`. Legacy `localStorage.userId` removed. |
| `3cd9f6c` | Added `MoneyMatters_Codebase_Analysis.md`, updated `CLAUDE.md`, added `setupAxiosInterceptor.js`, polished `main.jsx` Clerk wiring.                            |
| `63e0719` | Clerk provider mounted in the frontend + **portfolio stock autocomplete** using `nifty500.json` (3502 rows) with exchange detection and Yahoo-symbol prefill.  |

---

## 16. Quick-Start Checklist (today)

1. Copy `.env.example` → `.env`; fill in `SPRING_DATASOURCE_*`, `CLERK_JWK_SET_URI`, `CLERK_ISSUER_URI`, `CORS_ALLOWED_ORIGINS`, `NVIDIA_API_KEY`.
2. Copy `frontend/.env.example` → `frontend/.env`; set `VITE_CLERK_PUBLISHABLE_KEY`.
3. Backend: on Windows run `.\run-backend.ps1`; on *nix run `set -a && source .env && set +a && mvn spring-boot:run`.
4. Frontend: `cd frontend && npm install && npm run dev`.
5. Hit `http://localhost:5173`, sign in with Clerk, add a holding (autocomplete will suggest NSE/BSE symbols), and click **Analyse my portfolio** on the Portfolio page to exercise the full stack end-to-end.
