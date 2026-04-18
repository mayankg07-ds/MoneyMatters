package com.moneymatters.portfolio.repository;

import com.moneymatters.portfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    Optional<Holding> findByClerkUserIdAndAssetSymbol(String clerkUserId, String assetSymbol);

    List<Holding> findByClerkUserId(String clerkUserId);

    @Query("SELECT h FROM Holding h WHERE h.clerkUserId = :clerkUserId AND h.active = true")
    List<Holding> findActiveHoldingsByClerkUserId(@Param("clerkUserId") String clerkUserId);

    List<Holding> findByClerkUserIdAndAssetType(String clerkUserId, Holding.AssetType assetType);

    List<Holding> findByAssetSymbol(String assetSymbol);

    boolean existsByClerkUserIdAndAssetSymbol(String clerkUserId, String assetSymbol);
}
