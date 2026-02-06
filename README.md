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