package com.zeynep.productapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Stok hareketi entity'si; "stock_movements" tablosuna karsilik gelir.
 * Bir urune yapilan her stok girisi / cikisi burada bir satir olarak saklanir.
 */
@Entity
@Table(name = "stock_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MovementType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 255)
    private String note;

    /**
     * Bu hareket uygulandiktan sonraki stok.
     * Gecmis listelenirken her satirda o anki stogu gosterebilmek icin saklanir.
     */
    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;

    @CreationTimestamp
    private LocalDateTime movementDate;
}
