# Week 4 Learnings - Smart Loan Analyzer

## Financial Concepts

### Amortization Deep Dive
1. **Interest front-loaded**: Early payments are mostly interest
2. **Principal accelerates**: Late payments are mostly principal
3. **Fixed EMI magic**: Despite changing split, total payment stays same

### Prepayment Strategy
- **Reduce Tenure**: Best for those who want to be debt-free faster
- **Reduce EMI**: Best for improving monthly cash flow
- **Both save ~same interest** over loan life
- **Early prepayment = maximum impact**

## Technical Learnings

### Iterative Calculations
Unlike Week 2-3's closed-form formulas, amortization requires:
- Month-by-month loop
- Balance tracking
- Cumulative aggregation

### State Management
```java
BigDecimal balance = principal;  // Mutable state
for (int month = 1; month <= tenure; month++) {
    // Calculate this month
    // Update balance for next iteration
}
```

### Prepayment Complexity
- Must recalculate EMI or tenure mid-loop
- Handle multiple prepayments in sequence
- Ensure balance never goes negative

## Code Quality

### Helper Method Extraction
Original monolithic method → 6 focused helpers:
- `generateAmortizationSchedule()`
- `handlePrepayment()`
- `recalculateEMI()`
- `calculatePrepaymentImpact()`
- `generatePrincipalVsInterestChart()`
- `generateBalanceOverTimeChart()`

Each does ONE thing well.

### Test Strategy
- **Unit tests**: Pure math verification
- **Integration tests**: Full schedule accuracy
- **Edge cases**: Short/long tenure, excessive prepayment
- **Comparison tests**: Multiple loans

## Key Formulas

### Monthly Interest
```
Interest = Balance × (Annual Rate / 12 / 100)
```

### Principal Paid
```
Principal = EMI - Interest
```

### New Balance
```
Balance = Balance - Principal Paid
```

### Recalculate EMI (after prepayment)
```
New EMI = New Balance × [r(1+r)^n] / [(1+r)^n - 1]
Where n = remaining months
```

## Performance Considerations

### Amortization Schedule Size
- 30-year loan = 360 rows
- Each row: 9 fields (BigDecimal)
- Memory usage: ~5KB per loan
- Response size: ~50KB JSON

For long tenures, consider:
- Sampling (every 12 months)
- Pagination
- Summary-only mode

## Real-World Insights

### Indian Context
- **Home Loans**: Typically 20-30 years at 8-9%
- **Car Loans**: 5-7 years at 9-11%
- **Personal Loans**: 1-5 years at 12-16%

### Prepayment Rules
- Most banks allow prepayment after 6 months
- Some charge 2-3% penalty
- Our calculator doesn't model penalties (yet)

## Week 4 Statistics
- **Lines of code**: ~1,000
- **Helper methods**: 6
- **Unit tests**: 8
- **Commits**: 4
- **DTOs**: 6

## What's Next

### Week 5: Remaining calculators
- Asset Allocation
- Cashflow Planner
- SWP Generator
