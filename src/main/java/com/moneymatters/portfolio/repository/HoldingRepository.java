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

    Optional<Holding> findByUserIdAndAssetSymbol(String userId, String assetSymbol);

    List<Holding> findByUserId(String userId);

    @Query("SELECT h FROM Holding h WHERE h.userId = :userId AND h.active = true")
    List<Holding> findActiveHoldingsByUserId(@Param("userId") String userId);

    List<Holding> findByUserIdAndAssetType(String userId, Holding.AssetType assetType);

    List<Holding> findByAssetSymbol(String assetSymbol);

    boolean existsByUserIdAndAssetSymbol(String userId, String assetSymbol);
}
    