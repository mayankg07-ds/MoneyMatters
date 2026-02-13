# Portfolio Module API Documentation

> **MoneyMatters Financial Platform** - Complete Portfolio Management API Reference

## Base URL
```
http://localhost:8082/api/v1/portfolio
```

- **Port:** `8082`
- **Context Path:** `/api`
- **API Version:** `v1`
- **Module:** `portfolio`

---

## 📊 HOLDINGS API

Base Path: `/v1/portfolio/holdings`

### 1. Create Holding
**POST** `/v1/portfolio/holdings`

Creates a new holding for a user. This automatically creates an initial BUY transaction for FIFO tracking.

**Request Body:**
```json
{
  "userId": 1,
  "assetType": "STOCK",
  "assetName": "Reliance Industries",
  "assetSymbol": "RELIANCE",
  "exchange": "NSE",
  "quantity": 100,
  "avgBuyPrice": 2500.00,
  "purchaseDate": "2024-01-15"
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| userId | Long | Yes | User identifier |
| assetType | String | Yes | STOCK, MUTUAL_FUND, ETF, BOND, GOLD |
| assetName | String | Yes | Full name of the asset |
| assetSymbol | String | Yes | Trading symbol (e.g., RELIANCE, TCS) |
| exchange | String | Yes | NSE or BSE |
| quantity | BigDecimal | Yes | Number of units |
| avgBuyPrice | BigDecimal | Yes | Average purchase price per unit |
| purchaseDate | LocalDate | No | Date of purchase (defaults to today) |

**Response: (201 Created)**
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
    "quantity": 100.000000,
    "avgBuyPrice": 2500.00,
    "totalInvested": 250000.00,
    "currentPrice": 2800.00,
    "currentValue": 280000.00,
    "unrealizedGain": 30000.00,
    "unrealizedGainPercent": 12.00,
    "purchaseDate": "2024-01-15",
    "lastUpdated": "2024-01-15T10:30:00"
  },
  "message": "Holding created successfully"
}
```

**Notes:**
- Current price is fetched from Yahoo Finance automatically
- If price unavailable, uses avgBuyPrice as fallback
- Creates initial BUY transaction for FIFO tracking
- **Cache eviction:** Clears portfolio analytics cache for the user

---

### 2. Get Holding by ID
**GET** `/v1/portfolio/holdings/{id}`

Retrieves a specific holding by its ID.

**Path Parameters:**
- `id` (Long) - Holding identifier

**Response: (200 OK)**
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
    "quantity": 100.000000,
    "avgBuyPrice": 2500.00,
    "totalInvested": 250000.00,
    "currentPrice": 2850.00,
    "currentValue": 285000.00,
    "unrealizedGain": 35000.00,
    "unrealizedGainPercent": 14.00,
    "purchaseDate": "2024-01-15",
    "lastUpdated": "2024-03-10T15:45:00"
  },
  "message": "Holding retrieved successfully"
}
```

---

### 3. Get All Holdings for User
**GET** `/v1/portfolio/holdings/user/{userId}`

Retrieves all active holdings for a specific user.

**Path Parameters:**
- `userId` (Long) - User identifier

**Response: (200 OK)**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "assetType": "STOCK",
      "assetSymbol": "RELIANCE",
      "assetName": "Reliance Industries",
      "quantity": 150.000000,
      "avgBuyPrice": 2550.00,
      "totalInvested": 382500.00,
      "currentPrice": 2850.00,
      "currentValue": 427500.00,
      "unrealizedGain": 45000.00,
      "unrealizedGainPercent": 11.76
    },
    {
      "id": 2,
      "userId": 1,
      "assetType": "STOCK",
      "assetSymbol": "TCS",
      "assetName": "Tata Consultancy Services",
      "quantity": 50.000000,
      "avgBuyPrice": 3400.00,
      "totalInvested": 170000.00,
      "currentPrice": 3650.00,
      "currentValue": 182500.00,
      "unrealizedGain": 12500.00,
      "unrealizedGainPercent": 7.35
    }
  ],
  "message": "2 holdings found"
}
```

---

### 4. Get Portfolio Summary
**GET** `/v1/portfolio/holdings/user/{userId}/summary`

Generates comprehensive portfolio summary with asset allocation breakdown.

**Path Parameters:**
- `userId` (Long) - User identifier

**Response: (200 OK)**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "totalInvested": 552500.00,
    "totalCurrentValue": 610000.00,
    "totalUnrealizedGain": 57500.00,
    "totalUnrealizedGainPercent": 10.41,
    "totalHoldings": 3,
    "holdings": [
      {
        "id": 1,
        "assetSymbol": "RELIANCE",
        "assetName": "Reliance Industries",
        "assetType": "STOCK",
        "quantity": 150.000000,
        "totalInvested": 382500.00,
        "currentValue": 427500.00,
        "unrealizedGain": 45000.00,
        "unrealizedGainPercent": 11.76
      },
      {
        "id": 2,
        "assetSymbol": "TCS",
        "assetName": "Tata Consultancy Services",
        "assetType": "STOCK",
        "quantity": 50.000000,
        "totalInvested": 170000.00,
        "currentValue": 182500.00,
        "unrealizedGain": 12500.00,
        "unrealizedGainPercent": 7.35
      },
      {
        "id": 3,
        "assetSymbol": "HDFCBANK",
        "assetName": "HDFC Bank",
        "assetType": "STOCK",
        "quantity": 100.000000,
        "totalInvested": 150000.00,
        "currentValue": 155000.00,
        "unrealizedGain": 5000.00,
        "unrealizedGainPercent": 3.33
      }
    ],
    "assetTypeBreakdown": [
      {
        "assetType": "STOCK",
        "totalInvested": 552500.00,
        "currentValue": 610000.00,
        "allocation": 100.00,
        "count": 3
      }
    ]
  },
  "message": "Portfolio summary generated successfully"
}
```

**Asset Type Breakdown Fields:**
- `assetType`: Type of asset (STOCK, MUTUAL_FUND, ETF, etc.)
- `totalInvested`: Total amount invested in this asset type
- `currentValue`: Current market value
- `allocation`: Percentage allocation in portfolio
- `count`: Number of holdings in this category

---

### 5. Update Holding
**PUT** `/v1/portfolio/holdings/{id}`

Updates an existing holding's quantity and average buy price.

**Path Parameters:**
- `id` (Long) - Holding identifier

**Request Body:**
```json
{
  "userId": 1,
  "assetType": "STOCK",
  "assetName": "Reliance Industries",
  "assetSymbol": "RELIANCE",
  "exchange": "NSE",
  "quantity": 150,
  "avgBuyPrice": 2550.00,
  "purchaseDate": "2024-01-15"
}
```

**Response: (200 OK)**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "assetSymbol": "RELIANCE",
    "quantity": 150.000000,
    "avgBuyPrice": 2550.00,
    "totalInvested": 382500.00,
    "currentValue": 427500.00,
    "unrealizedGain": 45000.00,
    "unrealizedGainPercent": 11.76
  },
  "message": "Holding updated successfully"
}
```

**Notes:**
- Recalculates totalInvested automatically
- Fetches latest current price
- **Cache eviction:** Clears portfolio analytics cache for the user

---

### 6. Delete Holding
**DELETE** `/v1/portfolio/holdings/{id}`

Deletes a holding from the portfolio.

**Path Parameters:**
- `id` (Long) - Holding identifier

**Response: (200 OK)**
```json
{
  "success": true,
  "data": null,
  "message": "Holding deleted successfully"
}
```

**Notes:**
- Permanently removes the holding
- Associated transactions remain in history
- **Cache eviction:** Clears portfolio analytics cache for the user

---

### 7. Refresh Holding Price
**POST** `/v1/portfolio/holdings/{id}/refresh-price`

Fetches the latest price from Yahoo Finance and updates the holding.

**Path Parameters:**
- `id` (Long) - Holding identifier

**Response: (200 OK)**
```json
{
  "success": true,
  "data": null,
  "message": "Price refreshed successfully"
}
```

**Notes:**
- Updates currentPrice, currentValue, unrealizedGain
- Uses Yahoo Finance API
- **Cache eviction:** Clears portfolio analytics cache for the user

---

### 8. Refresh All User Prices
**POST** `/v1/portfolio/holdings/user/{userId}/refresh-prices`

Batch updates all holding prices for a user in a single operation.

**Path Parameters:**
- `userId` (Long) - User identifier

**Response: (200 OK)**
```json
{
  "success": true,
  "data": null,
  "message": "All prices refreshed successfully"
}
```

**Notes:**
- Efficient batch operation (single Yahoo Finance API call)
- Updates all holdings at once
- **Recommended** for refreshing entire portfolio
- **Cache eviction:** Clears portfolio analytics cache for the user

---

## 💰 TRANSACTIONS API

Base Path: `/v1/portfolio/transactions`

### 1. Record Transaction
**POST** `/v1/portfolio/transactions`

Records a new transaction and automatically updates the corresponding holding.

#### 1.1 BUY Transaction
Adds shares to existing holding or creates a new one. Recalculates average buy price using FIFO.

**Request:**
```json
{
  "userId": 1,
  "transactionType": "BUY",
  "assetType": "STOCK",
  "assetName": "Reliance Industries",
  "assetSymbol": "RELIANCE",
  "exchange": "NSE",
  "quantity": 50,
  "pricePerUnit": 2700.00,
  "charges": 150.00,
  "transactionDate": "2024-03-10",
  "notes": "Additional purchase"
}
```

**Response: (201 Created)**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "userId": 1,
    "transactionType": "BUY",
    "assetType": "STOCK",
    "assetName": "Reliance Industries",
    "assetSymbol": "RELIANCE",
    "exchange": "NSE",
    "quantity": 50.000000,
    "pricePerUnit": 2700.00,
    "totalAmount": 135000.00,
    "charges": 150.00,
    "netAmount": 135150.00,
    "transactionDate": "2024-03-10",
    "notes": "Additional purchase",
    "createdAt": "2024-03-10T14:30:00"
  },
  "message": "Transaction recorded successfully"
}
```

**BUY Logic:**
- If holding exists: Updates quantity and recalculates avgBuyPrice
- If holding doesn't exist: Creates new holding
- Average buy price calculation: `(oldInvested + newAmount) / (oldQty + newQty)`
- Charges are added to totalInvested but NOT to avgBuyPrice

---

#### 1.2 SELL Transaction
Reduces holding quantity and calculates realized gain using FIFO method.

**Request:**
```json
{
  "userId": 1,
  "transactionType": "SELL",
  "assetType": "STOCK",
  "assetName": "Reliance Industries",
  "assetSymbol": "RELIANCE",
  "exchange": "NSE",
  "quantity": 120,
  "pricePerUnit": 2900.00,
  "charges": 300.00,
  "transactionDate": "2024-06-15",
  "notes": "Partial exit"
}
```

**Response: (201 Created)**
```json
{
  "success": true,
  "data": {
    "id": 3,
    "userId": 1,
    "transactionType": "SELL",
    "assetType": "STOCK",
    "assetSymbol": "RELIANCE",
    "quantity": 120.000000,
    "pricePerUnit": 2900.00,
    "totalAmount": 348000.00,
    "charges": 300.00,
    "netAmount": 347700.00,
    "realizedGain": 56000.00,
    "realizedGainPercent": 18.67,
    "transactionDate": "2024-06-15",
    "notes": "Partial exit"
  },
  "message": "Transaction recorded successfully"
}
```

**SELL Logic:**
- Reduces holding quantity
- Calculates FIFO-based realized gain automatically
- Updates holding avgBuyPrice (removes oldest purchases first)
- If quantity becomes zero, holding remains with 0 quantity
- Error if insufficient quantity

---

#### 1.3 DIVIDEND Transaction
Records dividend income without affecting holding quantity.

**Request:**
```json
{
  "userId": 1,
  "transactionType": "DIVIDEND",
  "assetType": "STOCK",
  "assetName": "Reliance Industries",
  "assetSymbol": "RELIANCE",
  "exchange": "NSE",
  "quantity": 100,
  "pricePerUnit": 10.00,
  "charges": 0,
  "transactionDate": "2024-07-01",
  "notes": "Dividend ₹10/share"
}
```

**Response: (201 Created)**
```json
{
  "success": true,
  "data": {
    "id": 4,
    "userId": 1,
    "transactionType": "DIVIDEND",
    "assetSymbol": "RELIANCE",
    "quantity": 100.000000,
    "pricePerUnit": 10.00,
    "totalAmount": 1000.00,
    "netAmount": 1000.00,
    "transactionDate": "2024-07-01",
    "notes": "Dividend ₹10/share"
  },
  "message": "Transaction recorded successfully"
}
```

**DIVIDEND Logic:**
- No impact on holding quantity or avgBuyPrice
- Recorded for income tracking
- Used in portfolio analytics

---

#### 1.4 BONUS Transaction
Adds bonus shares and adjusts average buy price proportionally.

**Request:**
```json
{
  "userId": 1,
  "transactionType": "BONUS",
  "assetType": "STOCK",
  "assetName": "Reliance Industries",
  "assetSymbol": "RELIANCE",
  "exchange": "NSE",
  "quantity": 30,
  "pricePerUnit": 0,
  "charges": 0,
  "transactionDate": "2024-08-01",
  "notes": "Bonus 1:5"
}
```

**BONUS Logic:**
- Increases quantity
- avgBuyPrice = `(totalInvested) / (oldQty + bonusQty)`
- No cost, pure value addition

---

#### 1.5 SPLIT Transaction
Adjusts quantity and price after stock split.

**Request:**
```json
{
  "userId": 1,
  "transactionType": "SPLIT",
  "assetType": "STOCK",
  "assetName": "Reliance Industries",
  "assetSymbol": "RELIANCE",
  "exchange": "NSE",
  "quantity": 150,
  "pricePerUnit": 0,
  "charges": 0,
  "transactionDate": "2024-09-01",
  "notes": "Stock split 1:2"
}
```

**SPLIT Logic:**
- Increases quantity (e.g., 100 → 200 for 1:2 split)
- avgBuyPrice adjusted proportionally
- Total investment remains unchanged

---

**Common Notes for All Transactions:**
- All transactions trigger **cache eviction** for portfolio analytics
- Transactions are immutable once created (delete-only modification)
- Transaction date can be backdated for historical records
- Holdings are automatically updated based on transaction type

---

### 2. Get User Transactions
**GET** `/v1/portfolio/transactions/user/{userId}`

Retrieves all transactions for a user, ordered by date (newest first).

**Path Parameters:**
- `userId` (Long) - User identifier

**Response: (200 OK)**
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "userId": 1,
      "transactionType": "SELL",
      "assetSymbol": "RELIANCE",
      "quantity": 120.000000,
      "pricePerUnit": 2900.00,
      "totalAmount": 348000.00,
      "realizedGain": 56000.00,
      "transactionDate": "2024-06-15"
    },
    {
      "id": 2,
      "userId": 1,
      "transactionType": "BUY",
      "assetSymbol": "RELIANCE",
      "quantity": 50.000000,
      "pricePerUnit": 2700.00,
      "totalAmount": 135000.00,
      "transactionDate": "2024-03-10"
    },
    {
      "id": 1,
      "userId": 1,
      "transactionType": "BUY",
      "assetSymbol": "RELIANCE",
      "quantity": 100.000000,
      "pricePerUnit": 2500.00,
      "totalAmount": 250000.00,
      "transactionDate": "2024-01-15"
    }
  ],
  "message": "3 transactions found"
}
```

---

### 3. Get Transactions by Symbol
**GET** `/v1/portfolio/transactions/user/{userId}/symbol/{assetSymbol}`

Retrieves all transactions for a specific asset symbol.

**Path Parameters:**
- `userId` (Long) - User identifier
- `assetSymbol` (String) - Asset symbol (e.g., RELIANCE, TCS)

**Response: (200 OK)**
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "transactionType": "SELL",
      "assetSymbol": "RELIANCE",
      "quantity": 120.000000,
      "pricePerUnit": 2900.00,
      "realizedGain": 56000.00,
      "transactionDate": "2024-06-15"
    },
    {
      "id": 2,
      "transactionType": "BUY",
      "assetSymbol": "RELIANCE",
      "quantity": 50.000000,
      "pricePerUnit": 2700.00,
      "transactionDate": "2024-03-10"
    },
    {
      "id": 1,
      "transactionType": "BUY",
      "assetSymbol": "RELIANCE",
      "quantity": 100.000000,
      "pricePerUnit": 2500.00,
      "transactionDate": "2024-01-15"
    }
  ],
  "message": "3 transactions found for RELIANCE"
}
```

---

### 4. Get Transactions by Date Range
**GET** `/v1/portfolio/transactions/user/{userId}/date-range?startDate={startDate}&endDate={endDate}`

Retrieves transactions within a specific date range.

**Path Parameters:**
- `userId` (Long) - User identifier

**Query Parameters:**
- `startDate` (LocalDate) - Start date in ISO format (YYYY-MM-DD)
- `endDate` (LocalDate) - End date in ISO format (YYYY-MM-DD)

**Example:**
```
GET /v1/portfolio/transactions/user/1/date-range?startDate=2024-01-01&endDate=2024-12-31
```

**Response: (200 OK)**
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "transactionType": "SELL",
      "assetSymbol": "RELIANCE",
      "transactionDate": "2024-06-15",
      "totalAmount": 348000.00,
      "realizedGain": 56000.00
    },
    {
      "id": 2,
      "transactionType": "BUY",
      "assetSymbol": "RELIANCE",
      "transactionDate": "2024-03-10",
      "totalAmount": 135000.00
    }
  ],
  "message": "2 transactions found"
}
```

---

### 5. Calculate FIFO Gain
**GET** `/v1/portfolio/transactions/user/{userId}/symbol/{assetSymbol}/fifo?quantity={quantity}&salePrice={salePrice}`

Calculates expected realized gain for a future SELL transaction using FIFO (First In First Out) method.

**Path Parameters:**
- `userId` (Long) - User identifier
- `assetSymbol` (String) - Asset symbol

**Query Parameters:**
- `quantity` (BigDecimal) - Quantity to sell
- `salePrice` (BigDecimal) - Expected sale price per unit

**Example:**
```
GET /v1/portfolio/transactions/user/1/symbol/RELIANCE/fifo?quantity=120&salePrice=2900
```

**Response: (200 OK)**
```json
{
  "success": true,
  "data": {
    "totalRealizedGain": 56000.00,
    "totalRealizedGainPercent": 18.67,
    "totalSaleValue": 348000.00,
    "totalCostBasis": 292000.00,
    "batches": [
      {
        "transactionId": 1,
        "purchaseDate": "2024-01-15",
        "quantitySold": 100,
        "purchasePrice": 2500.00,
        "salePrice": 2900.00,
        "costBasis": 250000.00,
        "saleValue": 290000.00,
        "gain": 40000.00,
        "gainPercent": 16.00
      },
      {
        "transactionId": 2,
        "purchaseDate": "2024-03-10",
        "quantitySold": 20,
        "purchasePrice": 2700.00,
        "salePrice": 2900.00,
        "costBasis": 54000.00,
        "saleValue": 58000.00,
        "gain": 4000.00,
        "gainPercent": 7.41
      }
    ]
  },
  "message": "FIFO calculation completed"
}
```

**FIFO Calculation Details:**
- **batches**: Array of purchase batches used (oldest first)
- **transactionId**: Original BUY transaction ID
- **purchaseDate**: Date of purchase for that batch
- **quantitySold**: Shares sold from this batch
- **purchasePrice**: Original purchase price per share
- **costBasis**: Total cost = quantitySold × purchasePrice
- **saleValue**: Total sale = quantitySold × salePrice
- **gain**: Profit/Loss for this batch
- **gainPercent**: (gain / costBasis) × 100

**Use Case:**
- **MUST** call this endpoint before recording SELL transaction
- Helps user understand tax implications
- Shows which purchase batches will be used
- Calculates exact capital gains

---

### 6. Delete Transaction
**DELETE** `/v1/portfolio/transactions/{transactionId}`

Deletes a transaction from history.

**Path Parameters:**
- `transactionId` (Long) - Transaction identifier

**Response: (200 OK)**
```json
{
  "success": true,
  "data": null,
  "message": "Transaction deleted successfully"
}
```

**Notes:**
- Permanently removes transaction
- **Does NOT** reverse holding updates automatically
- Manual holding adjustment may be required
- Use with caution

---

## 📈 ANALYTICS API

Base Path: `/v1/portfolio/analytics`

### 1. Get Portfolio Analytics
**GET** `/v1/portfolio/analytics/user/{userId}`

Generates comprehensive portfolio performance analytics including XIRR, CAGR, and returns.

**Path Parameters:**
- `userId` (Long) - User identifier

**Response: (200 OK)**
```json
{
  "success": true,
  "data": {
    "totalInvested": 385000.00,
    "currentValue": 435000.00,
    "totalGain": 50000.00,
    "totalGainPercent": 12.99,
    "realizedGain": 0.00,
    "unrealizedGain": 50000.00,
    "xirr": 16.45,
    "absoluteReturn": 12.99,
    "cagr": 12.50,
    "firstInvestmentDate": "2024-01-15",
    "lastTransactionDate": "2024-06-15",
    "investmentDurationDays": 365,
    "investmentDurationYears": 1.0,
    "assetWiseAnalytics": [
      {
        "assetType": "STOCK",
        "invested": 385000.00,
        "currentValue": 435000.00,
        "gain": 50000.00,
        "gainPercent": 12.99,
        "allocation": 100.00,
        "count": 2
      }
    ],
    "topGainers": [
      {
        "assetSymbol": "RELIANCE",
        "assetName": "Reliance Industries",
        "invested": 250000.00,
        "currentValue": 285000.00,
        "gain": 35000.00,
        "gainPercent": 14.00
      },
      {
        "assetSymbol": "TCS",
        "assetName": "Tata Consultancy Services",
        "invested": 135000.00,
        "currentValue": 150000.00,
        "gain": 15000.00,
        "gainPercent": 11.11
      }
    ],
    "topLosers": [],
    "totalDividendReceived": 1000.00
  },
  "message": "Portfolio analytics generated successfully"
}
```

**Response Fields Explained:**

| Field | Description |
|-------|-------------|
| totalInvested | Total amount invested (including charges) |
| currentValue | Current market value of all holdings |
| totalGain | Total profit/loss (realized + unrealized) |
| totalGainPercent | Total return percentage |
| realizedGain | Profit from SELL transactions (locked-in) |
| unrealizedGain | Profit from current holdings (not sold) |
| **xirr** | Extended Internal Rate of Return (annualized) |
| **absoluteReturn** | Simple return percentage |
| **cagr** | Compound Annual Growth Rate |
| firstInvestmentDate | Date of first investment |
| lastTransactionDate | Date of most recent transaction |
| investmentDurationDays | Days since first investment |
| investmentDurationYears | Years since first investment |
| assetWiseAnalytics | Breakdown by asset type |
| topGainers | Best performing assets (sorted by % gain) |
| topLosers | Worst performing assets |
| totalDividendReceived | Sum of all DIVIDEND transactions |

**Performance Metrics:**

1. **XIRR (Extended Internal Rate of Return)**
   - Annualized return considering timing of all cash flows
   - Uses Newton-Raphson method for calculation
   - More accurate for irregular investments (SIP-like patterns)
   - Formula: `Σ(CF / (1+r)^(days/365)) = 0`

2. **CAGR (Compound Annual Growth Rate)**
   - Annualized return assuming lump sum investment
   - Formula: `((Ending Value / Beginning Value)^(1/years) - 1) × 100`
   - Good for comparing with benchmark indices

3. **Absolute Return**
   - Simple percentage return
   - Formula: `((Current Value - Invested) / Invested) × 100`
   - Doesn't consider time factor

**Caching:**
- ✅ **Response is cached** for performance
- Cache key: `userId`
- Cache evicted on: Any transaction, holding update, price refresh
- First call: ~200-500ms (full calculation)
- Subsequent calls: ~5-10ms (from cache)
- **20-100x performance improvement**

---

### 2. Get Analytics for Date Range
**GET** `/v1/portfolio/analytics/user/{userId}/date-range?startDate={startDate}&endDate={endDate}`

Generates analytics for a specific time period.

**Path Parameters:**
- `userId` (Long) - User identifier

**Query Parameters:**
- `startDate` (LocalDate) - Start date (ISO format: YYYY-MM-DD)
- `endDate` (LocalDate) - End date (ISO format: YYYY-MM-DD)

**Example:**
```
GET /v1/portfolio/analytics/user/1/date-range?startDate=2024-01-01&endDate=2024-06-30
```

**Response: (200 OK)**
```json
{
  "success": true,
  "data": {
    "totalInvested": 385000.00,
    "currentValue": 420000.00,
    "totalGain": 35000.00,
    "totalGainPercent": 9.09,
    "xirr": 14.25,
    "cagr": 11.80,
    "firstInvestmentDate": "2024-01-15",
    "lastTransactionDate": "2024-06-15",
    "investmentDurationDays": 182,
    "investmentDurationYears": 0.5
  },
  "message": "Portfolio analytics generated successfully"
}
```

**Use Cases:**
- Quarterly performance reports
- Tax year calculations (April-March in India)
- Custom period analysis
- Year-over-year comparisons

---

## 💹 STOCK PRICE API

Base Path: `/v1/portfolio/prices`

### 1. Get Current Price
**GET** `/v1/portfolio/prices/current/{symbol}?exchange={exchange}`

Fetches the current market price for a stock from Yahoo Finance.

**Path Parameters:**
- `symbol` (String) - Stock symbol (e.g., RELIANCE, TCS, INFY)

**Query Parameters:**
- `exchange` (String) - Exchange code (NSE or BSE), default: NSE

**Example:**
```
GET /v1/portfolio/prices/current/RELIANCE?exchange=NSE
```

**Response: (200 OK)**
```json
{
  "success": true,
  "data": {
    "symbol": "RELIANCE",
    "price": 2845.50
  },
  "message": "Price fetched successfully"
}
```

**Response (Price Not Found):**
```json
{
  "success": false,
  "data": null,
  "message": "Price not found for symbol: INVALID"
}
```

**Yahoo Symbol Convention:**
- NSE: `{symbol}.NS` (e.g., RELIANCE.NS)
- BSE: `{symbol}.BO` (e.g., RELIANCE.BO)

---

### 2. Get Stock Details
**GET** `/v1/portfolio/prices/details/{symbol}?exchange={exchange}`

Fetches comprehensive stock information including OHLC, volume, and 52-week range.

**Path Parameters:**
- `symbol` (String) - Stock symbol

**Query Parameters:**
- `exchange` (String) - Exchange code (NSE or BSE), default: NSE

**Example:**
```
GET /v1/portfolio/prices/details/RELIANCE?exchange=NSE
```

**Response: (200 OK)**
```json
{
  "success": true,
  "data": {
    "symbol": "RELIANCE.NS",
    "name": "Reliance Industries Limited",
    "currentPrice": 2845.50,
    "previousClose": 2830.25,
    "open": 2835.00,
    "dayHigh": 2860.00,
    "dayLow": 2820.00,
    "change": 15.25,
    "changePercent": 0.54,
    "volume": 5234567,
    "yearHigh": 3050.00,
    "yearLow": 2150.00
  },
  "message": "Stock details fetched successfully"
}
```

**Field Descriptions:**
- `symbol`: Yahoo Finance symbol (with exchange suffix)
- `name`: Full company name
- `currentPrice`: Current market price (LTP)
- `previousClose`: Yesterday's closing price
- `open`: Today's opening price
- `dayHigh`: Today's highest price
- `dayLow`: Today's lowest price
- `change`: Price change from previous close
- `changePercent`: Percentage change
- `volume`: Trading volume (number of shares)
- `yearHigh`: 52-week high
- `yearLow`: 52-week low

---

### 3. Update Holding Price
**POST** `/v1/portfolio/prices/update/holding/{holdingId}`

Refreshes price for a specific holding.

**Path Parameters:**
- `holdingId` (Long) - Holding identifier

**Response: (200 OK)**
```json
{
  "success": true,
  "data": "Price updated successfully for holding 1",
  "message": "Price updated"
}
```

**Notes:**
- Fetches from Yahoo Finance
- Updates holding's currentPrice, currentValue, unrealizedGain
- **Cache eviction:** Clears portfolio analytics cache

---

### 4. Update All User Prices
**POST** `/v1/portfolio/prices/update/user/{userId}`

Batch updates prices for all holdings of a user.

**Path Parameters:**
- `userId` (Long) - User identifier

**Response: (200 OK)**
```json
{
  "success": true,
  "data": "Prices updated successfully for 5 holdings",
  "message": "Batch price update completed"
}
```

**Notes:**
- **Efficient:** Single Yahoo Finance API call for all holdings
- Updates all holdings at once
- **Recommended** over individual price updates
- **Cache eviction:** Clears portfolio analytics cache once at end

---

## 📊 Transaction Types Reference

| Type | Description | Holdings Impact | Use Case |
|------|-------------|-----------------|----------|
| **BUY** | Purchase shares | ✅ Adds/updates quantity<br>✅ Recalculates avgBuyPrice<br>✅ Updates totalInvested | Regular purchases, SIPs |
| **SELL** | Sell shares | ✅ Reduces quantity<br>✅ Calculates FIFO gain<br>✅ Updates avgBuyPrice | Profit booking, exits |
| **DIVIDEND** | Dividend received | ❌ No holding change<br>✅ Recorded as income | Dividend payouts |
| **BONUS** | Bonus shares | ✅ Increases quantity<br>✅ Adjusts avgBuyPrice | 1:1, 1:2 bonus issues |
| **SPLIT** | Stock split | ✅ Multiplies quantity<br>✅ Divides avgBuyPrice | 1:2, 1:5 splits |

---

## 🔑 Key Financial Concepts

### XIRR (Extended Internal Rate of Return)
Annualized return that accounts for the timing and amount of all cash flows.

**Formula:**
```
Σ(CF_i / (1 + r)^(days_i / 365)) = 0
```

Where:
- `CF_i` = Cash flow (negative for investments, positive for returns)
- `r` = Rate of return (XIRR)
- `days_i` = Days from first investment to cash flow date

**Calculation Method:**
- Uses Newton-Raphson iterative method
- Precision: 0.0001 (0.01%)
- Max iterations: 1000

**Use Case:**
- SIP-like investments
- Irregular cash flow patterns
- Multiple entry/exit points
- Most accurate return metric

---

### FIFO (First In First Out)
Tax calculation method where oldest purchases are sold first.

**Example:**
```
Purchases:
- Jan 15: 100 shares @ ₹2500 = ₹250,000
- Mar 10: 50 shares @ ₹2700 = ₹135,000

Sale: 120 shares @ ₹2900
Using FIFO:
- Sell 100 from Jan 15 batch: Gain = (2900-2500)×100 = ₹40,000
- Sell 20 from Mar 10 batch: Gain = (2900-2700)×20 = ₹4,000
- Total Gain = ₹44,000
```

**Why FIFO?**
- Mandatory in India for tax purposes
- Ensures accurate capital gains calculation
- Transparent cost basis tracking

---

### CAGR (Compound Annual Growth Rate)
Smoothed annual return assuming lump sum investment.

**Formula:**
```
CAGR = ((Ending Value / Beginning Value)^(1 / years) - 1) × 100
```

**Example:**
```
Initial Investment: ₹100,000
Final Value: ₹150,000
Duration: 3 years

CAGR = ((150000/100000)^(1/3) - 1) × 100 = 14.47%
```

**Use Case:**
- Comparing fund performance
- Benchmarking against indices
- Long-term growth rate

---

### Absolute Return
Simple percentage return without time consideration.

**Formula:**
```
Return % = ((Current Value - Invested) / Invested) × 100
```

**Example:**
```
Invested: ₹100,000
Current Value: ₹125,000

Absolute Return = ((125000 - 100000) / 100000) × 100 = 25%
```

**Use Case:**
- Quick return check
- Short-term investments
- Simple comparison

---

## ⚠️ Error Responses

### Error Format
```json
{
  "success": false,
  "data": null,
  "message": "Error description here"
}
```

### HTTP Status Codes

| Code | Status | Description | Example Scenario |
|------|--------|-------------|------------------|
| **200** | OK | Request successful | GET requests, successful updates |
| **201** | Created | Resource created | POST /holdings, POST /transactions |
| **400** | Bad Request | Invalid input data | Missing required fields, invalid format |
| **404** | Not Found | Resource doesn't exist | GET /holdings/999 (non-existent ID) |
| **409** | Conflict | Duplicate resource | Creating holding that already exists |
| **500** | Internal Server Error | Server-side error | Database connection issues, API failures |

### Common Error Messages

#### 400 Bad Request
```json
{
  "success": false,
  "data": null,
  "message": "Validation failed: quantity must be greater than 0"
}
```

#### 404 Not Found
```json
{
  "success": false,
  "data": null,
  "message": "Holding not found with ID: 123"
}
```

#### 409 Conflict
```json
{
  "success": false,
  "data": null,
  "message": "Holding already exists for this symbol. Use update instead."
}
```

#### 500 Internal Server Error
```json
{
  "success": false,
  "data": null,
  "message": "Failed to fetch price from Yahoo Finance"
}
```

---

## 🎯 Best Practices

### 1. Transaction Workflow
```
✅ RECOMMENDED FLOW:
1. Calculate FIFO gain (GET /transactions/.../fifo)
2. Review expected gains/losses
3. Record SELL transaction (POST /transactions)
4. Check updated portfolio (GET /analytics/user/{userId})
```

### 2. Price Management
```
✅ USE BATCH UPDATES:
- Single holding: POST /holdings/{id}/refresh-price
- Multiple holdings: POST /holdings/user/{userId}/refresh-prices (PREFERRED)
```

### 3. Performance Optimization
```
✅ LEVERAGE CACHING:
- First analytics call: ~500ms (calculation)
- Subsequent calls: ~10ms (cached)
- Cache auto-clears on data changes
```

### 4. Data Accuracy
```
✅ MAINTAIN CONSISTENCY:
- Record ALL transactions (don't skip)
- Keep transaction dates accurate
- Update prices regularly
- Use correct transaction types
```

### 5. Error Handling
```
✅ HANDLE ERRORS GRACEFULLY:
- Check "success" field in response
- Display user-friendly error messages
- Retry failed price fetches
- Validate input before API calls
```

### 6. FIFO Calculation
```
✅ ALWAYS CALCULATE BEFORE SELLING:
- Understand tax implications
- See which batches will be used
- Plan tax-efficient exits
- Avoid surprises
```

### 7. Analytics Usage
```
✅ COMPREHENSIVE INSIGHTS:
- Use XIRR for irregular investments
- Use CAGR for benchmark comparison
- Check asset allocation regularly
- Monitor top gainers/losers
```

---

## 📊 Example Workflows

### Workflow 1: Complete Investment Journey

```bash
# Step 1: Create Initial Holding (BUY 100 shares @ ₹2500)
POST /v1/portfolio/holdings
{
  "userId": 1,
  "assetType": "STOCK",
  "assetSymbol": "RELIANCE",
  "quantity": 100,
  "avgBuyPrice": 2500
}

# Step 2: Record Additional Purchase (BUY 50 @ ₹2700)
POST /v1/portfolio/transactions
{
  "userId": 1,
  "transactionType": "BUY",
  "assetSymbol": "RELIANCE",
  "quantity": 50,
  "pricePerUnit": 2700
}

# Step 3: Calculate FIFO before selling (120 shares @ ₹2900)
GET /v1/portfolio/transactions/user/1/symbol/RELIANCE/fifo?quantity=120&salePrice=2900

# Step 4: Review FIFO calculation
# - Expected gain: ₹56,000
# - Using 2 batches: 100 from Jan + 20 from Mar
# - Tax planning completed

# Step 5: Record SELL transaction
POST /v1/portfolio/transactions
{
  "userId": 1,
  "transactionType": "SELL",
  "assetSymbol": "RELIANCE",
  "quantity": 120,
  "pricePerUnit": 2900
}

# Step 6: Check Portfolio Summary
GET /v1/portfolio/holdings/user/1/summary

# Step 7: View Comprehensive Analytics
GET /v1/portfolio/analytics/user/1
```

---

### Workflow 2: Daily Portfolio Management

```bash
# Morning: Refresh all prices
POST /v1/portfolio/prices/update/user/1

# Check current status
GET /v1/portfolio/holdings/user/1/summary

# View performance metrics
GET /v1/portfolio/analytics/user/1

# Record any transactions during the day
POST /v1/portfolio/transactions

# Evening: Review day's changes
GET /v1/portfolio/analytics/user/1
```

---

### Workflow 3: Tax Planning (Year-End)

```bash
# Step 1: Get all transactions for financial year
GET /v1/portfolio/transactions/user/1/date-range?startDate=2024-04-01&endDate=2025-03-31

# Step 2: Check realized gains (from SELL transactions)
GET /v1/portfolio/analytics/user/1/date-range?startDate=2024-04-01&endDate=2025-03-31

# Step 3: Plan future sales using FIFO
GET /v1/portfolio/transactions/user/1/symbol/RELIANCE/fifo?quantity=50&salePrice=3000

# Step 4: Execute tax-efficient sales
POST /v1/portfolio/transactions
```

---

### Workflow 4: Performance Review

```bash
# Quarter 1 Performance
GET /v1/portfolio/analytics/user/1/date-range?startDate=2024-01-01&endDate=2024-03-31

# Quarter 2 Performance
GET /v1/portfolio/analytics/user/1/date-range?startDate=2024-04-01&endDate=2024-06-30

# Full Year Performance
GET /v1/portfolio/analytics/user/1/date-range?startDate=2024-01-01&endDate=2024-12-31

# Current Overall Performance
GET /v1/portfolio/analytics/user/1
```

---

## 🚀 Performance Features

### Caching Implementation
- **Technology:** Spring Cache with ConcurrentMapCacheManager
- **Cached Endpoints:** 
  - `GET /v1/portfolio/analytics/user/{userId}`
- **Cache Key:** `userId`
- **Performance Improvement:** 20-100x faster
- **First Call:** ~200-500ms (full XIRR calculation)
- **Cached Calls:** ~5-10ms (memory retrieval)

### Automatic Cache Eviction
Cache is automatically cleared on:
- ✅ Transaction recorded (BUY/SELL/DIVIDEND/BONUS/SPLIT)
- ✅ Holding created/updated/deleted
- ✅ Price refreshed (single or batch)
- ✅ User-level granularity (only affected user's cache cleared)

### Database Indexes
Optimized queries with indexes on:
- **Holdings:** `(userId, assetSymbol)`, `assetType`
- **Transactions:** `(userId, transactionDate)`, `holdingId`, `transactionType`
- **Query Performance:** 10-100x faster

---

## 📝 Additional Notes

### Supported Asset Types
- `STOCK` - Equity shares
- `MUTUAL_FUND` - Mutual fund units
- `ETF` - Exchange Traded Funds
- `BOND` - Fixed income securities
- `GOLD` - Gold/Silver investments

### Supported Exchanges
- `NSE` - National Stock Exchange (India)
- `BSE` - Bombay Stock Exchange (India)

### Date Format
All dates use ISO 8601 format: `YYYY-MM-DD`
Example: `2024-01-15`

### Decimal Precision
- Prices: 2 decimal places (e.g., 2845.50)
- Quantities: 6 decimal places (e.g., 100.000000)
- Percentages: 2 decimal places (e.g., 14.25)

### Currency
All monetary values are in Indian Rupees (₹) by default.

---

## 🔗 Related Documentation

- [Performance Optimizations](../PERFORMANCE_OPTIMIZATIONS.md) - Caching and indexing details
- [Calculator APIs](../calculators/README.md) - Financial calculators (SIP, SWP, Retirement, etc.)
- [Database Schema](../docs/DATABASE_SCHEMA.md) - Entity relationships and schema

---

## 📞 Support

For issues or questions:
- GitHub Issues: [Create an issue](https://github.com/your-repo/issues)
- Email: support@moneymatters.com
- Documentation: [Full docs](https://docs.moneymatters.com)

---

**Last Updated:** February 12, 2026  
**API Version:** v1  
**Platform:** MoneyMatters Financial Management System
