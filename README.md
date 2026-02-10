# MoneyMatters

A financial project.

## All 6 Calculators Complete

1. **SIP Step-up** - `/v1/calculators/sip-stepup/calculate`
2. **Retirement Planner** - `/v1/calculators/retirement/plan`
3. **Loan Analyzer** - `/v1/calculators/loan/analyze`
4. **Asset Allocation** - `/v1/calculators/asset-allocation/rebalance`
5. **Cashflow Planner** - `/v1/calculators/cashflow/project`
6. **SWP Calculator** - `/v1/calculators/swp/calculate`

---

## SIP Step-up Calculator API

**Endpoint**

`POST /api/v1/calculators/sip-stepup/calculate`

**Request Body**

```json
{
  "monthlySIP": 10000,
  "expectedAnnualReturnPercent": 12,
  "years": 3,
  "annualStepupPercent": 10
}
```

**Response**

```json
{
  "success": true,
  "data": {
    "totalInvested": 397200.00,
    "maturityValue": 468740.35,
    "wealthGained": 71540.35,
    "firstYearMonthlySIP": 10000.00,
    "lastYearMonthlySIP": 12100.00,
    "yearlyBreakdown": [
      {
        "year": 1,
        "monthlySIP": 10000.00,
        "yearlyContribution": 120000.00,
        "valueAtYearEnd": 126800.00,
        "valueAtMaturity": 159000.00
      }
    ],
    "maturityCurve": [
      { "label": "Year 1", "value": 159000.00 }
    ]
  },
  "message": "SIP Step-up calculation successful",
  "timestamp": 1700000000000
}
```

## Retirement Planner API

**Endpoint**

`POST /api/v1/calculators/retirement/plan`

**Request Body**

```json
{
  "currentAge": 30,
  "retirementAge": 60,
  "lifeExpectancy": 85,
  "currentMonthlyExpense": 50000,
  "expectedInflationPercent": 6,
  "expectedReturnPreRetirementPercent": 12,
  "expectedReturnPostRetirementPercent": 8,
  "existingCorpus": 1000000
}
```

**Response**

```json
{
  "success": true,
  "data": {
    "inflatedMonthlyExpenseAtRetirement": 287174.56,
    "inflatedAnnualExpenseAtRetirement": 3446094.72,
    "requiredCorpusAtRetirement": 37800000.00,
    "projectedExistingCorpusAtRetirement": 29960000.00,
    "corpusShortfall": 7840000.00,
    "recommendedMonthlySIP": 2200.00,
    "totalSIPInvestmentNeeded": 792000.00,
    "yearsToRetirement": 30,
    "yearsInRetirement": 25,
    "preRetirementProjections": [...],
    "postRetirementProjections": [...],
    "corpusGrowthChart": [...]
  },
  "message": "Retirement plan calculated successfully"
}
```

## Loan Analyzer API

### Analyze Single Loan

**Endpoint**

`POST /api/v1/calculators/loan/analyze`

**Request Body**

```json
{
  "principal": 500000,
  "annualInterestRatePercent": 10,
  "tenureMonths": 60,
  "prepayments": [
    {
      "atMonth": 12,
      "amount": 50000,
      "option": "REDUCE_TENURE"
    }
  ]
}
```

**Response Highlights**

- **EMI**: ₹10,624
- **Total Interest**: ₹1,14,742 (after prepayment savings)
- **Full amortization schedule**: 54 monthly breakdowns (reduced from 60)
- **Prepayment Impact**: 
  - Tenure reduced by 6 months
  - Interest saved: ₹22,669
  - Original total cost: ₹6,37,411
  - New total cost: ₹6,14,742

**Response Structure**

```json
{
  "success": true,
  "data": {
    "emi": 10623.52,
    "totalAmountPayable": 614742.16,
    "totalInterestPayable": 114742.16,
    "effectiveTenureMonths": 54,
    "principal": 500000.00,
    "interestToLoanPercentage": 22.95,
    "amortizationSchedule": [
      {
        "month": 1,
        "emi": 10623.52,
        "principalPaid": 6456.85,
        "interestPaid": 4166.67,
        "prepaymentAmount": 0.00,
        "closingBalance": 493543.15
      }
    ],
    "prepaymentImpact": {
      "totalPrepaymentAmount": 50000.00,
      "interestSaved": 22669.16,
      "monthsSaved": 6,
      "newEMI": 10623.52,
      "originalTotalCost": 637411.20,
      "newTotalCost": 614742.16
    },
    "principalVsInterestChart": [...],
    "balanceOverTimeChart": [...]
  },
  "message": "Loan analysis completed successfully"
}
```

**Prepayment Options**

- `REDUCE_TENURE`: Keep EMI same, reduce loan duration
- `REDUCE_EMI`: Keep tenure same, reduce monthly EMI