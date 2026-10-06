package com.zeynep.productapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Urun bilgisini tasir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Urun bilgisi")
public class ProductResponse {

    @Schema(description = "Urun ID", example = "1")
    private Long id;

    @Schema(description = "Urun adi", example = "Mekanik Klavye")
    private String name;

    @Schema(description = "Urun aciklamasi", example = "RGB isikli, mavi switch")
    private String description;

    @Schema(description = "Kategori ID", example = "1")
    private Long categoryId;

    @Schema(description = "Kategori adi", example = "Elektronik")
    private String categoryName;

    @Schema(description = "Tedarikci ID", example = "1")
    private Long supplierId;

    @Schema(description = "Tedarikci adi", example = "Anadolu Teknoloji Dagitim A.S.")
    private String supplierName;

    @Schema(description = "Urun fiyati (TL)", example = "1450.00")
    private BigDecimal price;

    @Schema(description = "Stok adedi", example = "25")
    private Integer stock;

    /** Entity'de yoktur; cevap uretilirken stok esigine gore hesaplanir. */
    @Schema(description = "Stok esigin altinda mi?", example = "false")
    private boolean lowStock;

    @Schema(description = "Kaydin olusturuldugu an", example = "2026-10-05T14:32:07")
    private LocalDateTime createdAt;

    @Schema(description = "Kaydin son guncellendigi an", example = "2026-10-05T14:32:07")
    private LocalDateTime updatedAt;
}