package com.moneymatters.portfolio.repository;

import com.moneymatters.portfolio.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByClerkUserId(String clerkUserId);

    List<Transaction> findByClerkUserIdOrderByTransactionDateDesc(String clerkUserId);

    List<Transaction> findByClerkUserIdAndAssetSymbol(String clerkUserId, String assetSymbol);

    @Query("SELECT t FROM Transaction t WHERE t.clerkUserId = :clerkUserId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByClerkUserIdAndDateRange(
        @Param("clerkUserId") String clerkUserId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.clerkUserId = :clerkUserId " +
           "AND t.assetSymbol = :assetSymbol " +
           "AND t.transactionType = 'BUY' " +
           "ORDER BY t.transactionDate ASC")
    List<Transaction> findBuyTransactionsForAsset(
        @Param("clerkUserId") String clerkUserId,
        @Param("assetSymbol") String assetSymbol);
}
