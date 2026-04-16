# MoneyMatters — Codebase Analysis Report

**Date:** April 10, 2026
**Project:** MoneyMatters — Indian Personal Finance Platform
**Stack:** Spring Boot 3.2 (Java 17) + React 19 (Vite) + H2 Database

---

## 1. Project Summary

MoneyMatters is a full-stack Indian personal finance platform with two major modules:

**Financial Calculators** — Six stateless backend calculators (SIP Step-up, Retirement Planner, Loan Analyzer, Asset Allocation Rebalancer, Cashflow Planner, SWP Calculator) plus three frontend-only calculators (FD, RD, PPF). Each calculator accepts user inputs via POST requests and returns detailed projections, charts data, and breakdowns without persisting any state.

**Portfolio Management System** — A complete CRUD-based portfolio tracker that lets users manage stock/MF/ETF/bond/gold holdings, record transactions (buy, sell, dividend, bonus, split), fetch live prices from Yahoo Finance, and view performance analytics including XIRR, CAGR, and asset-wise P&L. Prices are cached and auto-refreshed every 15 minutes during NSE trading hours.

The frontend is a React 19 SPA with Clerk authentication, a responsive sidebar layout, Chart.js visualizations, and a polished landing page with animations. The backend follows a clean layered architecture (Controller → Service → Repository → Entity) with global exception handling and a consistent API response wrapper.

---

## 2. Architecture Overview

```
┌──────────────────────────────────────────────────────────┐
│                    FRONTEND (React 19 + Vite)            │
│  ┌──────────┐ ┌───────────┐ ┌──────────┐ ┌───────────┐  │
│  │Dashboard │ │ Portfolio │ │Analytics │ │Calculators│  │
│  └────┬─────┘ └─────┬─────┘ └────┬─────┘ └─────┬─────┘  │
│       └──────────────┴───────────┴──────────────┘        │
│                      api.js (Axios)                      │
│                    Clerk Auth Guard                       │
└────────────────────────┬─────────────────────────────────┘
                         │ HTTP (CORS: localhost:5173/3000)
┌────────────────────────┴─────────────────────────────────┐
│              BACKEND (Spring Boot 3.2, Port 8082)        │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ Controllers: SIP, Retirement, Loan, Asset, Cashflow,│ │
│  │ SWP, Holding, Transaction, Analytics, StockPrice    │ │
│  └──────────────────────┬──────────────────────────────┘ │
│  ┌──────────────────────┴──────────────────────────────┐ │
│  │ Services: Calculator logic, FIFO engine, XIRR       │ │
│  │ solver, Yahoo Finance client, Price scheduler        │ │
│  └──────────────────────┬──────────────────────────────┘ │
│  ┌──────────────────────┴────────────┐ ┌──────────────┐  │
│  │ Spring Data JPA Repositories      │ │ Spring Cache │  │
│  │ (Holding, Transaction)            │ │ (stockPrices,│  │
│  └──────────────────────┬────────────┘ │ analytics)   │  │
│                         │              └──────────────┘  │
│  ┌──────────────────────┴────────────────────────────┐   │
│  │        H2 In-Memory Database (Dev)                │   │
│  │  Tables: holding, transaction                     │   │
│  │  Indexes: (userId,assetSymbol), (userId,txnDate)  │   │
│  └───────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
                         │
              ┌──────────┴──────────┐
              │  Yahoo Finance v8   │
              │  (Live Price Feed)  │
              └─────────────────────┘
```

**Key stats:** ~85 Java source files, 38+ JSX components, 30+ API endpoints, 15 test files, 6 composite database indexes, and comprehensive documentation across 9 files plus 16 Figma design mockups.

---

## 3. What's Working Well

The project demonstrates strong fundamentals in several areas:

**Clean separation of concerns.** Every module follows Controller → Service (interface + impl) → Repository → Entity. DTOs are used for all request/response payloads, keeping entities isolated from the API layer.

**Financial math accuracy.** The Newton-Raphson XIRR solver, Apache Commons Math integration, and custom FinancialMathService handle compound interest, EMI, annuity, and present value calculations correctly. The FIFO cost-basis engine tracks multiple buy lots per holding.

**Caching and scheduling.** Stock prices are cached with a 24-hour TTL and auto-refreshed during market hours. Portfolio analytics cache is evicted on any write mutation, preventing stale data.

**Consistent API design.** Every endpoint returns `{success, data, message, timestamp}` via a shared `ApiResponse` wrapper. Global exception handling catches validation errors, not-found, and unexpected exceptions with proper HTTP status codes.

**Frontend polish.** The landing page uses Motion animations, a live ticker, and well-designed CTA sections. The dashboard shows KPI cards, charts, and recent activity. Tables are sortable, filterable, and paginated.

**Documentation.** API docs cover 50+ endpoints with example payloads. Weekly learning journals track architectural decisions. Performance optimization notes explain index strategy and cache benchmarks.

---

## 4. Gaps and Issues

### 4.1 Security — Critical

**No backend authentication.** The userId is passed as a path parameter or hardcoded as `1` in the frontend. Any user can access any other user's portfolio by changing the userId in the URL. Clerk handles frontend auth, but the backend has zero validation — no JWT verification, no session tokens, no Spring Security at all.

**CORS is wide open for dev.** Allowing `localhost:5173` and `localhost:3000` is fine for development, but there's no production CORS configuration, no origin whitelisting strategy, and no CSRF protection.

**No input sanitization beyond bean validation.** While `@Valid` covers field-level constraints, there's no protection against SQL injection via JPA query methods, no rate limiting on API calls, and no request size limits.

### 4.2 Database — Significant

**H2 in-memory only.** All data is lost on every restart. There are no migration scripts (Flyway/Liquibase), no seed data, and no production database configuration. The `application.yml` mentions PostgreSQL readiness but nothing is configured.

**No user table.** Users exist only as a `userId` long field on holdings and transactions. There's no user entity, no profile, no preferences storage.

**No audit trail.** Transaction deletion has no reversal logic (logged as "not fully implemented"). There's no soft-delete on transactions, no change history, and no data recovery mechanism.

### 4.3 Business Logic — Moderate

**Transaction reversal is stubbed.** Deleting a transaction doesn't reverse its effect on the holding (quantity, average price). This can corrupt portfolio data silently.

**Date-range analytics delegates to full analytics.** The date-filtered endpoint calls the same service method as the unfiltered one, ignoring the date parameters entirely.

**Dashboard uses simulated data.** Month-over-month portfolio performance on the dashboard is generated with random fluctuations, not computed from actual transaction history.

**FD, RD, PPF calculators have no backend.** These three calculators compute everything in React, meaning the logic can't be reused, tested with JUnit, or exposed via API.

### 4.4 Infrastructure — Moderate

**No CI/CD pipeline.** No GitHub Actions, no Dockerfile, no docker-compose, no deployment scripts.

**No environment configuration.** Single `application.yml` with no profiles for dev/staging/production. Database credentials, API keys, and CORS origins are hardcoded.

**Logging is basic.** File logging to `logs/moneymatters.log` with no rotation policy, no structured logging (JSON), no correlation IDs for request tracing.

### 4.5 Frontend — Minor to Moderate

**Settings page is empty.** The route exists with no implementation — no user preferences, no theme settings, no notification controls.

**No error boundaries.** A crash in any component takes down the entire app. There are no React error boundaries, no fallback UIs.

**No loading skeletons.** API calls show no intermediate state — the UI either shows stale data or nothing while fetching.

**No offline handling.** If the backend is down, the frontend shows raw Axios errors with no user-friendly messaging.

### 4.6 Testing — Minor

**No frontend tests.** Playwright is installed as a dependency but there are no test files for the React app. No unit tests for components, no integration tests for user flows.

**Backend integration tests are minimal.** Only 2 integration tests exist. Edge cases like concurrent transactions, FIFO with splits/bonuses, and cache invalidation timing aren't covered.

---

## 5. What to Build Next (Priority Roadmap)

### Phase 1 — Foundation Fixes (Week 1–2)

These are blockers that should be addressed before adding any new features:

**1. Backend Authentication with Spring Security + JWT.** Integrate Clerk's JWT verification on the backend. Add a security filter that extracts the userId from the JWT token instead of trusting path parameters. This is the single most important fix — without it, the app has zero data isolation between users.

**2. PostgreSQL Migration.** Replace H2 with PostgreSQL. Add Flyway migration scripts for schema versioning. Create a `users` table linked to Clerk's user IDs. Set up `application-dev.yml` and `application-prod.yml` profiles.

**3. Transaction Reversal Logic.** When a transaction is deleted, reverse its effect on the holding: recalculate average buy price, adjust quantity, update unrealized gains. Add soft-delete to transactions with a `deletedAt` timestamp.

**4. Fix Date-Range Analytics.** Implement actual date filtering in the analytics service — filter transactions by date range before computing XIRR, CAGR, and P&L.

### Phase 2 — Production Readiness (Week 3–4)

**5. Docker + CI/CD.** Create a `Dockerfile` for the Spring Boot app and a `docker-compose.yml` with PostgreSQL + the app. Add a GitHub Actions pipeline for build, test, and deploy.

**6. Environment Configuration.** Externalize all config: database URLs, CORS origins, Yahoo Finance settings, Clerk keys. Use Spring profiles and environment variables.

**7. Rate Limiting and API Security.** Add request rate limiting (e.g., Bucket4j or Spring Cloud Gateway), request size limits, and API key management for any future third-party integrations.

**8. Error Boundaries and Loading States.** Add React error boundaries to every route. Implement skeleton loading screens for dashboard, portfolio, and analytics pages. Add a global error handler for Axios that shows user-friendly toast messages.

### Phase 3 — Feature Expansion (Week 5–8)

**9. Watchlist Module.** Let users track stocks they don't own — with price alerts, 52-week high/low notifications, and custom notes. This is a natural extension of the existing StockPrice service.

**10. Goal-Based Planning.** Create a goal tracker where users set financial targets (house down payment, emergency fund, child's education) with target amounts, deadlines, and linked SIPs. Show progress as a percentage with projections.

**11. Tax Computation Engine.** Build an Indian capital gains tax calculator: short-term vs long-term classification based on holding period (1 year for equity, 3 years for debt), Section 80C deductions, tax-loss harvesting suggestions. This would tie into the existing FIFO gain calculator.

**12. Mutual Fund Integration.** Add NAV fetching from AMFI (Association of Mutual Funds in India), scheme search by name/AMFI code, and SIP tracking with actual vs expected return comparison.

**13. Real Dashboard Data.** Replace simulated month-over-month data with actual portfolio value snapshots. Create a `portfolio_snapshot` table that records daily/weekly portfolio value, enabling real historical performance charts.

### Phase 4 — Advanced Features (Week 9–12)

**14. PDF Report Generation.** Generate downloadable portfolio reports with holdings summary, P&L statement, asset allocation pie chart, and transaction history. Useful for CA filing and personal records.

**15. Multi-Currency Support.** Add USD, EUR support for users holding international stocks or US ETFs. Integrate an exchange rate API and store currency per holding.

**16. Notification System.** Price alerts (stock crosses a threshold), SIP reminders, portfolio rebalancing nudges, and weekly digest emails. Use Spring's event system with an email service (SendGrid/SES).

**17. Budget Tracker.** Extend the Cashflow Planner into a monthly budget tracker with income/expense categories, recurring entries, and spending trend visualization.

**18. AI-Powered Insights.** Use an LLM API to generate natural-language portfolio commentary: "Your portfolio gained 4.2% this month, led by TCS (+8.3%). Consider rebalancing — IT stocks now represent 45% of your portfolio vs your 30% target."

---

## 6. Quick Wins (Can Ship in a Day)

These small improvements deliver visible value with minimal effort:

- **Move FD/RD/PPF calculators to the backend** — reuse the existing calculator architecture and get testability for free.
- **Add CSV import for transactions** — parse a CSV file and bulk-create transactions, useful for users migrating from other trackers.
- **Implement the Settings page** — even basic preferences like default currency, date format, and dark/light theme toggle would complete the navigation.
- **Add a "total returns" banner on the portfolio page** — show total invested, current value, absolute gain, and XIRR in a sticky header bar.
- **Export portfolio summary as JSON/CSV** — the analytics endpoint already computes everything; just add a download button.

---

## 7. Tech Debt Summary

| Area | Issue | Severity | Effort |
|------|-------|----------|--------|
| Auth | No backend JWT verification | Critical | Medium |
| DB | H2 in-memory, no migrations | High | Medium |
| Logic | Transaction delete doesn't reverse holdings | High | Low |
| Logic | Date-range analytics ignores dates | Medium | Low |
| Logic | Dashboard uses fake data | Medium | Medium |
| Infra | No Docker/CI/CD | Medium | Medium |
| Infra | No env profiles | Medium | Low |
| Frontend | No error boundaries | Medium | Low |
| Frontend | No loading states | Low | Low |
| Frontend | Settings page empty | Low | Low |
| Testing | Zero frontend tests | Medium | High |
| Testing | Minimal integration tests | Low | Medium |

---

## 8. Final Assessment

MoneyMatters is a well-architected project with strong financial computation logic, clean code organization, and a polished UI. The calculator module is essentially production-ready — stateless, well-tested, and accurately implemented. The portfolio module has solid CRUD, FIFO tracking, and live price integration that most personal finance apps charge for.

The critical path to production readiness is short: backend authentication, PostgreSQL, and transaction reversal fixes would address the three biggest gaps. After that, the project is a strong foundation for features like goal tracking, tax computation, and mutual fund integration that would make it genuinely useful for Indian retail investors.

The codebase quality — consistent patterns, good documentation, performance optimizations — suggests it can scale well as features are added.
