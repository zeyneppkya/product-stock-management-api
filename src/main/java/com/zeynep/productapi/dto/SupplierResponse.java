package com.zeynep.productapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tedarikci bilgisini tasir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tedarikci bilgisi")
public class SupplierResponse {

    @Schema(description = "Tedarikci ID", example = "1")
    private Long id;

    @Schema(description = "Firma adi", example = "Anadolu Teknoloji Dagitim A.S.")
    private String name;

    @Schema(description = "Firmadaki yetkili kisi", example = "Ahmet Yilmaz")
    private String contactName;

    @Schema(description = "E-posta adresi", example = "satis@anadoluteknoloji.example")
    private String email;

    @Schema(description = "Telefon numarasi", example = "0212 555 01 01")
    private String phone;

    @Schema(description = "Adres", example = "Maslak Mah. Buyukdere Cad. No:12 Sariyer/Istanbul")
    private String address;

    @Schema(description = "Kaydin olusturuldugu an", example = "2026-10-05T14:32:07")
    private LocalDateTime createdAt;

    @Schema(description = "Kaydin son guncellendigi an", example = "2026-10-05T14:32:07")
    private LocalDateTime updatedAt;
}
