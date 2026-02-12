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

---

## Portfolio Holdings API

### Create Holding

**Endpoint**

`POST /api/v1/portfolio/holdings`

**Request Body**

```json
{
  "userId": 1,
  "assetType": "STOCK",
  "assetName": "Reliance Industries",
  "assetSymbol": "RELIANCE",
  "exchange": "NSE",
  "quantity": 10,
  "avgBuyPrice": 2800.50,
  "purchaseDate": "2024-01-15"
}
```

**Asset Types**: `STOCK`, `MUTUAL_FUND`, `ETF`, `BOND`, `GOLD`

**Response**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "assetType": "STOCK",
    "assetName": "Reliance Industries",
    "assetSymbol": "RELIANCE",
    "exchange": "NSE",
    "quantity": 10.0000,
    "avgBuyPrice": 2800.50,
    "totalInvested": 28005.00,
    "currentPrice": 1453.60,
    "currentValue": 14536.00,
    "unrealizedGain": -13469.00,
    "unrealizedGainPercent": -48.0950,
    "purchaseDate": "2024-01-15",
    "lastUpdated": "2026-02-12T12:42:53.18379"
  },
  "message": "Holding created successfully",
  "timestamp": 1770880455574
}
```

### Get Single Holding

**Endpoint**

`GET /api/v1/portfolio/holdings/{id}`

**Response**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "assetType": "STOCK",
    "assetName": "Reliance Industries",
    "assetSymbol": "RELIANCE",
    "exchange": "NSE",
    "quantity": 10.0000,
    "avgBuyPrice": 2800.50,
    "totalInvested": 28005.00,
    "currentPrice": 1453.60,
    "currentValue": 14536.00,
    "unrealizedGain": -13469.00,
    "unrealizedGainPercent": -48.0950,
    "purchaseDate": "2024-01-15",
    "lastUpdated": "2026-02-12T12:42:53.18379"
  },
  "message": "Holding retrieved successfully",
  "timestamp": 1770880447898
}
```

### Get User Holdings

**Endpoint**

`GET /api/v1/portfolio/holdings/user/{userId}`

**Response**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "assetType": "STOCK",
      "assetName": "Reliance Industries",
      "assetSymbol": "RELIANCE",
      "exchange": "NSE",
      "quantity": 10.0000,
      "avgBuyPrice": 2800.50,
      "totalInvested": 28005.00,
      "currentPrice": 1453.60,
      "currentValue": 14536.00,
      "unrealizedGain": -13469.00,
      "unrealizedGainPercent": -48.0950,
      "purchaseDate": "2024-01-15",
      "lastUpdated": "2026-02-12T12:42:53.18379"
    },
    {
      "id": 2,
      "userId": 1,
      "assetType": "STOCK",
      "assetName": "Tata Consultancy Services",
      "assetSymbol": "TCS",
      "exchange": "BSE",
      "quantity": 5.0000,
      "avgBuyPrice": 3500.00,
      "totalInvested": 17500.00,
      "currentPrice": 2766.40,
      "currentValue": 13832.00,
      "unrealizedGain": -3668.00,
      "unrealizedGainPercent": -20.9600,
      "purchaseDate": "2024-02-10",
      "lastUpdated": "2026-02-12T12:44:15.571605"
    }
  ],
  "message": "2 holdings found",
  "timestamp": 1770880437922
}
```

### Update Holding

**Endpoint**

`PUT /api/v1/portfolio/holdings/{id}`

**Request Body**

```json
{
  "userId": 1,
  "assetType": "STOCK",
  "assetName": "Reliance Industries",
  "assetSymbol": "RELIANCE",
  "exchange": "NSE",
  "quantity": 15,
  "avgBuyPrice": 2600.00
}
```

**Response**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "assetType": "STOCK",
    "assetName": "Reliance Industries",
    "assetSymbol": "RELIANCE",
    "exchange": "NSE",
    "quantity": 15,
    "avgBuyPrice": 2600.00,
    "totalInvested": 39000.00,
    "currentPrice": 1453.60,
    "currentValue": 21804.00,
    "unrealizedGain": -17196.00,
    "unrealizedGainPercent": -44.0923,
    "purchaseDate": "2024-01-15",
    "lastUpdated": "2026-02-12T12:44:42.9861854"
  },
  "message": "Holding updated successfully",
  "timestamp": 1770880483022
}
```

### Delete Holding

**Endpoint**

`DELETE /api/v1/portfolio/holdings/{id}`

**Response**

```json
{
  "success": true,
  "message": "Holding deleted successfully",
  "timestamp": 1770880506088
}
```

### Portfolio Summary

**Endpoint**

`GET /api/v1/portfolio/holdings/user/{userId}/summary`

**Response**

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "totalInvested": 45505.00,
    "totalCurrentValue": 28368.00,
    "totalUnrealizedGain": -17137.00,
    "totalUnrealizedGainPercent": -37.6596,
    "totalHoldings": 2,
    "holdings": [
      {
        "id": 1,
        "userId": 1,
        "assetType": "STOCK",
        "assetName": "Reliance Industries",
        "assetSymbol": "RELIANCE",
        "exchange": "NSE",
        "quantity": 10.0000,
        "avgBuyPrice": 2800.50,
        "totalInvested": 28005.00,
        "currentPrice": 1453.60,
        "currentValue": 14536.00,
        "unrealizedGain": -13469.00,
        "unrealizedGainPercent": -48.0950,
        "purchaseDate": "2024-01-15",
        "lastUpdated": "2026-02-12T12:42:53.18379"
      },
      {
        "id": 2,
        "userId": 1,
        "assetType": "STOCK",
        "assetName": "Tata Consultancy Services",
        "assetSymbol": "TCS",
        "exchange": "BSE",
        "quantity": 5.0000,
        "avgBuyPrice": 3500.00,
        "totalInvested": 17500.00,
        "currentPrice": 2766.40,
        "currentValue": 13832.00,
        "unrealizedGain": -3668.00,
        "unrealizedGainPercent": -20.9600,
        "purchaseDate": "2024-02-10",
        "lastUpdated": "2026-02-12T12:44:15.571605"
      }
    ],
    "assetTypeBreakdown": [
      {
        "assetType": "STOCK",
        "totalInvested": 45505.00,
        "currentValue": 28368.00,
        "allocation": 100.00,
        "count": 2
      }
    ]
  },
  "message": "Portfolio summary generated successfully",
  "timestamp": 1770880462341
}
```

### Refresh Single Holding Price

**Endpoint**

`POST /api/v1/portfolio/holdings/{id}/refresh-price`

**Response**

```json
{
  "success": true,
  "message": "Price refreshed successfully",
  "timestamp": 1770880490263
}
```

### Refresh All Holdings Prices

**Endpoint**

`POST /api/v1/portfolio/holdings/user/{userId}/refresh-prices`

**Response**

```json
{
  "success": true,
  "message": "All prices refreshed successfully",
  "timestamp": 1770880499298
}
```

---

## Stock Price API

### Get Current Price

**Endpoint**

`GET /api/v1/portfolio/prices/current/{symbol}?exchange={exchange}`

**Example**

`GET /api/v1/portfolio/prices/current/RELIANCE?exchange=NSE`

**Response**

```json
{
  "success": true,
  "data": {
    "symbol": "RELIANCE",
    "price": 1458.50
  },
  "message": "Price fetched successfully",
  "timestamp": 1770730428670
}
```

### Get Stock Details

**Endpoint**

`GET /api/v1/portfolio/prices/details/{symbol}?exchange={exchange}`

**Example**

`GET /api/v1/portfolio/prices/details/TCS?exchange=BSE`

**Response**

```json
{
  "success": true,
  "data": {
    "symbol": "TCS.BO",
    "name": "Tata Consultancy Services Limited",
    "currentPrice": 2984.25,
    "previousClose": 2947.10,
    "dayHigh": 3011.00,
    "dayLow": 2943.55,
    "change": 37.15,
    "changePercent": 1.26,
    "volume": 468333,
    "yearHigh": 4040.85,
    "yearLow": 2867.55
  },
  "message": "Stock details fetched successfully",
  "timestamp": 1770730436468
}
```

**Supported Exchanges**
- `NSE` - National Stock Exchange (format: `SYMBOL.NS`)
- `BSE` - Bombay Stock Exchange (format: `CODE.BO`)

**Examples**
- RELIANCE (NSE): `RELIANCE.NS`
- TCS (BSE): `532540.BO` or `TCS.BO`
- INFY (NSE): `INFY.NS`
- HDFC Bank (NSE): `HDFCBANK.NS`

---

## API Documentation

**Base URL**: `http://localhost:8082/api`

**Swagger UI**: `http://localhost:8082/api/swagger-ui/index.html`

**OpenAPI Spec**: `http://localhost:8082/api/v3/api-docs`

**Health Check**: `http://localhost:8082/api/actuator/health`