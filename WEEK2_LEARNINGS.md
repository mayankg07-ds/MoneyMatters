# Week 2 Learnings - SIP Step-up Calculator

## Concepts

1. Step-up SIP is just multiple regular SIPs with increasing amounts.
2. Each year's contributions behave like a 12-month annuity.
3. Then each year's maturity grows further until final year.

## Technical Learnings

- How to combine:
  - `calculateAnnuityFutureValue` for each year's monthly SIP.
  - `calculateFutureValue` to bring that to final maturity.
- Importance of using BigDecimal end-to-end for financial math.
- Designing DTOs that are frontend-friendly (yearly breakdown, chart points).
- Writing tests using ranges instead of exact equals for floating-like calculations.

## Gotchas

- Step-up factor must be computed as 1 + (step%/100).
- For 0% step-up, logic must behave exactly like normal SIP.
- For 0 or negative years/SIP, return zeros instead of crashing.