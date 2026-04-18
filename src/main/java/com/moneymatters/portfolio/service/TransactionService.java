package com.moneymatters.portfolio.service;

import com.moneymatters.portfolio.dto.FIFOCalculationResult;
import com.moneymatters.portfolio.dto.TransactionRequest;
import com.moneymatters.portfolio.dto.TransactionResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    
    TransactionResponse recordTransaction(String clerkUserId, TransactionRequest request);
    
    List<TransactionResponse> getUserTransactions(String userId);
    
    List<TransactionResponse> getTransactionsBySymbol(String userId, String assetSymbol);
    
    List<TransactionResponse> getTransactionsByDateRange(
        String userId, LocalDate startDate, LocalDate endDate);
    
    FIFOCalculationResult calculateFIFOGain(String userId, String assetSymbol, 
        BigDecimal quantityToSell, BigDecimal salePrice);
    
    void deleteTransaction(Long transactionId);
}
