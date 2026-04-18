package com.moneymatters.portfolio.controller;

import com.moneymatters.common.dto.ApiResponse;
import com.moneymatters.portfolio.dto.FIFOCalculationResult;
import com.moneymatters.portfolio.dto.TransactionRequest;
import com.moneymatters.portfolio.dto.TransactionResponse;
import com.moneymatters.portfolio.service.TransactionService;
import com.moneymatters.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> recordTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));
        log.info("Recording {} transaction for user {}", request.getTransactionType(), clerkUserId);

        TransactionResponse response = transactionService.recordTransaction(clerkUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, response, "Transaction recorded successfully"));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getUserTransactions(
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));

        List<TransactionResponse> transactions = transactionService.getUserTransactions(clerkUserId);

        return ResponseEntity.ok(new ApiResponse<>(true, transactions,
            transactions.size() + " transactions found"));
    }

    @GetMapping("/user/symbol/{assetSymbol}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsBySymbol(
            @PathVariable String assetSymbol,
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));

        List<TransactionResponse> transactions =
            transactionService.getTransactionsBySymbol(clerkUserId, assetSymbol);

        return ResponseEntity.ok(new ApiResponse<>(true, transactions,
            transactions.size() + " transactions found for " + assetSymbol));
    }

    @GetMapping("/user/date-range")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));

        List<TransactionResponse> transactions =
            transactionService.getTransactionsByDateRange(clerkUserId, startDate, endDate);

        return ResponseEntity.ok(new ApiResponse<>(true, transactions,
            transactions.size() + " transactions found"));
    }

    @GetMapping("/user/symbol/{assetSymbol}/fifo")
    public ResponseEntity<ApiResponse<FIFOCalculationResult>> calculateFIFOGain(
            @PathVariable String assetSymbol,
            @RequestParam BigDecimal quantity,
            @RequestParam BigDecimal salePrice,
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));

        FIFOCalculationResult result = transactionService.calculateFIFOGain(
            clerkUserId, assetSymbol, quantity, salePrice);

        return ResponseEntity.ok(new ApiResponse<>(true, result, "FIFO calculation completed"));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable Long transactionId,
            @AuthenticationPrincipal Jwt jwt) {

        userService.ensureUserExists(jwt.getSubject(), jwt.getClaimAsString("email"));

        transactionService.deleteTransaction(transactionId);

        return ResponseEntity.ok(new ApiResponse<>(true, null,
            "Transaction deleted successfully"));
    }
}
