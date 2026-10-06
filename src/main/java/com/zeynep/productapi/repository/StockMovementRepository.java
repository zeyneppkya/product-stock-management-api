package com.zeynep.productapi.repository;

import com.zeynep.productapi.model.MovementType;
import com.zeynep.productapi.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Stok hareketi veri erisim katmani.
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByProduct_Id(Long productId, Pageable pageable);

    Page<StockMovement> findByType(MovementType type, Pageable pageable);

    Page<StockMovement> findByProduct_IdAndType(Long productId, MovementType type, Pageable pageable);

    /**
     * Hareketin tarihini degistirir.
     * movementDate @CreationTimestamp oldugu icin normal save() ile degistirilemez.
     * SADECE baslangic verisini gecmise yaymak icin (DataLoader) kullanilir.
     */
    @Modifying
    @Transactional
    @Query("update StockMovement m set m.movementDate = :movementDate where m.id = :id")
    int updateMovementDate(@Param("id") Long id, @Param("movementDate") LocalDateTime movementDate);
}
