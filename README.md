# MoneyMatters

A financial project.

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