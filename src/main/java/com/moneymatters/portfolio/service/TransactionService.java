package com.moneymatters.portfolio.service;

import com.moneymatters.portfolio.dto.FIFOCalculationResult;
import com.moneymatters.portfolio.dto.TransactionRequest;
import com.moneymatters.portfolio.dto.TransactionResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    
    TransactionResponse recordTransaction(TransactionRequest request);
    
    List<TransactionResponse> getUserTransactions(Long userId);
    
    List<TransactionResponse> getTransactionsBySymbol(Long userId, String assetSymbol);
    
    List<TransactionResponse> getTransactionsByDateRange(
        Long userId, LocalDate startDate, LocalDate endDate);
    
    FIFOCalculationResult calculateFIFOGain(Long userId, String assetSymbol, 
        BigDecimal quantityToSell, BigDecimal salePrice);
    
    void deleteTransaction(Long transactionId);
}
