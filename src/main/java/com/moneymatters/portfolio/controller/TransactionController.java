package com.moneymatters.portfolio.controller;

import com.moneymatters.common.dto.ApiResponse;
import com.moneymatters.portfolio.dto.FIFOCalculationResult;
import com.moneymatters.portfolio.dto.TransactionRequest;
import com.moneymatters.portfolio.dto.TransactionResponse;
import com.moneymatters.portfolio.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/portfolio/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> recordTransaction(
            @Valid @RequestBody TransactionRequest request) {

        log.info("Recording {} transaction for user {}",
            request.getTransactionType(), request.getUserId());

        TransactionResponse response = transactionService.recordTransaction(request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, response, "Transaction recorded successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getUserTransactions(
            @PathVariable Long userId) {

        List<TransactionResponse> transactions = transactionService.getUserTransactions(userId);

        return ResponseEntity.ok(new ApiResponse<>(true, transactions,
            transactions.size() + " transactions found"));
    }

    @GetMapping("/user/{userId}/symbol/{assetSymbol}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsBySymbol(
            @PathVariable Long userId,
            @PathVariable String assetSymbol) {

        List<TransactionResponse> transactions = 
            transactionService.getTransactionsBySymbol(userId, assetSymbol);

        return ResponseEntity.ok(new ApiResponse<>(true, transactions,
            transactions.size() + " transactions found for " + assetSymbol));
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<TransactionResponse> transactions = 
            transactionService.getTransactionsByDateRange(userId, startDate, endDate);

        return ResponseEntity.ok(new ApiResponse<>(true, transactions,
            transactions.size() + " transactions found"));
    }

    @GetMapping("/user/{userId}/symbol/{assetSymbol}/fifo")
    public ResponseEntity<ApiResponse<FIFOCalculationResult>> calculateFIFOGain(
            @PathVariable Long userId,
            @PathVariable String assetSymbol,
            @RequestParam BigDecimal quantity,
            @RequestParam BigDecimal salePrice) {

        FIFOCalculationResult result = transactionService.calculateFIFOGain(
            userId, assetSymbol, quantity, salePrice);

        return ResponseEntity.ok(new ApiResponse<>(true, result,
            "FIFO calculation completed"));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable Long transactionId) {

        transactionService.deleteTransaction(transactionId);

        return ResponseEntity.ok(new ApiResponse<>(true, null,
            "Transaction deleted successfully"));
    }
}
