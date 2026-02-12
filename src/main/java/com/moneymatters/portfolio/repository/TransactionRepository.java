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

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByUserIdAndAssetSymbol(Long userId, String assetSymbol);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND t.assetSymbol = :assetSymbol " +
           "AND t.transactionType = 'BUY' " +
           "ORDER BY t.transactionDate ASC")
    List<Transaction> findBuyTransactionsForAsset(
        @Param("userId") Long userId,
        @Param("assetSymbol") String assetSymbol);
}
