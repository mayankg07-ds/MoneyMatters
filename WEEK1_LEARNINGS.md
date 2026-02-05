# Week 1 Learnings

## Day 5: CalculationUtils

### What I Learned

1. **BigDecimal is ESSENTIAL for financial calculations**
   - Never use float/double (precision loss)
   - Always set SCALE (decimal places) explicitly
   - Use RoundingMode.HALF_UP for banking

2. **Test-Driven Development (TDD)**
   - Write test FIRST with expected values
   - Then write code to pass test
   - Tests give confidence in correctness
   - Failing test shows you exactly what's wrong

3. **Power calculation appears everywhere**
   - FV = PV × (1+r)^n uses power()
   - EMI formula uses power()
   - Annuity formula uses power()
   - Test this utility well!

4. **Utility methods reduce duplication**
   - percentToDecimal() used by ALL calculators
   - format() used for all outputs
   - isPositive() for validation
   - Tested once, used many times

### Key Takeaway

**Big Decimal + RoundingMode.HALF_UP = Financial Reliability**

In banking, every rupee counts. Compound errors from float precision
can add up to significant money loss. Always use BigDecimal!
