import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8082/api/v1',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

// ─── Portfolio Holdings ───
export const holdingsApi = {
  create: (data) => API.post('/portfolio/holdings', data),
  update: (id, data) => API.put(`/portfolio/holdings/${id}`, data),
  delete: (id) => API.delete(`/portfolio/holdings/${id}`),
  getById: (id) => API.get(`/portfolio/holdings/${id}`),
  getByUser: (userId) => API.get(`/portfolio/holdings/user/${userId}`),
  getSummary: (userId) => API.get(`/portfolio/holdings/user/${userId}/summary`),
  refreshPrice: (id) => API.post(`/portfolio/holdings/${id}/refresh-price`),
  refreshAll: (userId) => API.post(`/portfolio/holdings/user/${userId}/refresh-prices`),
};

// ─── Transactions ───
export const transactionsApi = {
  record: (data) => API.post('/portfolio/transactions', data),
  getByUser: (userId) => API.get(`/portfolio/transactions/user/${userId}`),
  getBySymbol: (userId, symbol) => API.get(`/portfolio/transactions/user/${userId}/symbol/${symbol}`),
  getByDateRange: (userId, start, end) =>
    API.get(`/portfolio/transactions/user/${userId}/date-range`, { params: { startDate: start, endDate: end } }),
  calculateFIFO: (userId, symbol, qty, price) =>
    API.get(`/portfolio/transactions/user/${userId}/symbol/${symbol}/fifo`, { params: { quantity: qty, salePrice: price } }),
  delete: (id) => API.delete(`/portfolio/transactions/${id}`),
};

// ─── Portfolio Analytics ───
export const analyticsApi = {
  getByUser: (userId) => API.get(`/portfolio/analytics/user/${userId}`),
  getByDateRange: (userId, start, end) =>
    API.get(`/portfolio/analytics/user/${userId}/date-range`, { params: { startDate: start, endDate: end } }),
};

// ─── Stock Prices ───
export const pricesApi = {
  getCurrent: (symbol, exchange) => API.get(`/portfolio/prices/current/${symbol}`, { params: { exchange } }),
  getDetails: (symbol, exchange) => API.get(`/portfolio/prices/details/${symbol}`, { params: { exchange } }),
};

// ─── Calculators ───
export const calculatorsApi = {
  sipStepup: (data) => API.post('/calculators/sip-stepup/calculate', data),
  retirement: (data) => API.post('/calculators/retirement/plan', data),
  loanAnalyze: (data) => API.post('/calculators/loan/analyze', data),
  loanCompare: (data) => API.post('/calculators/loan/compare', data),
  assetAllocation: (data) => API.post('/calculators/asset-allocation/rebalance', data),
  cashflow: (data) => API.post('/calculators/cashflow/project', data),
  swp: (data) => API.post('/calculators/swp/calculate', data),
};

export default API;
