package com.zeynep.productapi.repository;

import com.zeynep.productapi.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Tedarikci veri erisim katmani.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
