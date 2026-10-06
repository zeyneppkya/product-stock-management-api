package com.zeynep.productapi.dto;

import com.zeynep.productapi.model.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stok hareketi (giris / cikis) olusturma isteginde kullanilir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stok hareketi olusturma istegi")
public class StockMovementRequest {

    @NotNull(message = "Urun ID zorunludur")
    @Schema(description = "Hareketin yapilacagi urunun ID'si", example = "1")
    private Long productId;

    @NotNull(message = "Hareket tipi zorunludur (IN veya OUT)")
    @Schema(description = "Hareket tipi: IN = stok girisi, OUT = stok cikisi", example = "IN")
    private MovementType type;

    @NotNull(message = "Miktar zorunludur")
    @Min(value = 1, message = "Miktar en az 1 olmalidir")
    @Schema(description = "Hareket miktari", example = "10")
    private Integer quantity;

    @Size(max = 255, message = "Not en fazla 255 karakter olabilir")
    @Schema(description = "Aciklama notu (zorunlu degil)", example = "Tedarikciden yeni sevkiyat")
    private String note;
}
