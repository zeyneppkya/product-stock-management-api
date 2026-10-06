package com.zeynep.productapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Urun olusturma ve guncelleme isteklerinde kullanilir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Urun olusturma / guncelleme istegi")
public class ProductRequest {

    @NotBlank(message = "Urun adi bos olamaz")
    @Size(min = 2, max = 100, message = "Urun adi 2 ile 100 karakter arasinda olmalidir")
    @Schema(description = "Urun adi", example = "Mekanik Klavye")
    private String name;

    @Size(max = 500, message = "Aciklama en fazla 500 karakter olabilir")
    @Schema(description = "Urun aciklamasi (zorunlu degil)", example = "RGB isikli, mavi switch")
    private String description;

    @NotNull(message = "Kategori ID zorunludur")
    @Schema(description = "Urunun ait oldugu kategorinin ID'si", example = "1")
    private Long categoryId;

    @NotNull(message = "Tedarikci ID zorunludur")
    @Schema(description = "Urunun tedarikcisinin ID'si", example = "1")
    private Long supplierId;

    @NotNull(message = "Fiyat zorunludur")
    @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat 0'dan buyuk olmalidir")
    @Digits(integer = 10, fraction = 2, message = "Fiyat en fazla 2 ondalik basamak icerebilir")
    @Schema(description = "Urun fiyati (TL)", example = "1450.00")
    private BigDecimal price;

    @NotNull(message = "Stok adedi zorunludur")
    @Min(value = 0, message = "Stok negatif olamaz")
    @Schema(description = "Stok adedi", example = "25")
    private Integer stock;
}
