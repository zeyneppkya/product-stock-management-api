package com.zeynep.productapi.dto;

import com.zeynep.productapi.model.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stok hareketi bilgisini tasir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stok hareketi bilgisi")
public class StockMovementResponse {

    @Schema(description = "Hareket ID", example = "1")
    private Long id;

    @Schema(description = "Urun ID", example = "1")
    private Long productId;

    @Schema(description = "Urun adi", example = "Mekanik Klavye")
    private String productName;

    @Schema(description = "Hareket tipi", example = "IN")
    private MovementType type;

    @Schema(description = "Hareket miktari", example = "10")
    private Integer quantity;

    @Schema(description = "Aciklama notu", example = "Tedarikciden yeni sevkiyat")
    private String note;

    @Schema(description = "Hareketin kaydedildigi an", example = "2026-10-05T14:32:07")
    private LocalDateTime movementDate;

    @Schema(description = "Bu hareketten sonraki stok adedi", example = "35")
    private Integer stockAfter;
}
