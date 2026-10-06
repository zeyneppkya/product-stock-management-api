package com.zeynep.productapi.repository;

import com.zeynep.productapi.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Urun veri erisim katmani.
 * JpaSpecificationExecutor sayesinde dinamik filtrelerle (Specification) arama yapilabilir.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    List<Product> findByCategory_NameIgnoreCase(String categoryName);

    List<Product> findByStockLessThan(Integer threshold);

    /**
     * Urunu satir kilidiyle getirir.
     * Stok hareketlerinde kullanilir: ayni urune ayni anda gelen iki istek
     * birbirinin stok guncellemesini ezmesin diye ikincisi birincinin bitmesini bekler.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
