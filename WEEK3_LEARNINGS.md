# Week 3 Learnings - Retirement Planner

## Financial Concepts Mastered

1. **Inflation Impact**: How 6% inflation over 30 years can 5x your expenses
2. **Corpus Calculation**: Using PVA to determine retirement corpus needed
3. **Reverse SIP**: Working backwards from target to required monthly investment
4. **Real vs Nominal Returns**: Post-retirement returns must beat inflation

## Technical Learnings

### Complex Service Design
- Breaking down multi-step financial calculations
- Validation at multiple levels (business logic + HTTP)
- Helper methods for clarity (calculateReverseSIP, generateProjections)

### Testing Strategy
- Using ranges instead of exact values for financial calculations
- Edge case testing (zero years, invalid ages, high inflation)
- Realistic scenarios from manual calculations

### Code Organization
- Service layer handles pure business logic
- Controller layer handles HTTP concerns only
- DTOs are rich (yearly projections, chart data)

## Formula Deep Dive

### Reverse SIP Formula

SIP = Target × r / [(1 + r)^n - 1]

Where:

Target = corpus shortfall

r = monthly return rate

n = total months to retirement

This is the inverse of the annuity FV formula we used in Week 2.

### Why Two Different Return Rates?
- Pre-retirement: Can take more risk (equity heavy) → 12-15%
- Post-retirement: Must be conservative (debt heavy) → 6-9%

## Key Insights

1. **Start Early**: 30-year-old needs ₹2,200/month. 45-year-old would need ₹15,000+/month.
2. **Inflation is Silent Killer**: 6% inflation doubles your expenses every 12 years.
3. **Existing Corpus Matters**: ₹10L today = ₹3 crores at retirement (12% for 30 years).
4. **Conservative in Retirement**: Can't take risks when you're withdrawing regularly.

## Gotchas Encountered

1. **Division by Zero**: When monthlyRate = 0, must handle separately.
2. **Negative Corpus**: Post-retirement projections can go negative if withdrawals too high.
3. **Validation Order**: Check retirement age > current age BEFORE calculations.
4. **BigDecimal Precision**: Critical for multi-decade calculations.

## Next Steps

Week 4 will build the Loan Analyzer with amortization schedules and prepayment impact analysis.