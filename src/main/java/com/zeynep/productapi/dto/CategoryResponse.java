package com.zeynep.productapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kategori bilgisini tasir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kategori bilgisi")
public class CategoryResponse {

    @Schema(description = "Kategori ID", example = "1")
    private Long id;

    @Schema(description = "Kategori adi", example = "Elektronik")
    private String name;

    @Schema(description = "Kategori aciklamasi",
            example = "Bilgisayar cevre birimleri ve elektronik cihazlar")
    private String description;

    @Schema(description = "Kaydin olusturuldugu an", example = "2026-10-05T14:32:07")
    private LocalDateTime createdAt;

    @Schema(description = "Kaydin son guncellendigi an", example = "2026-10-05T14:32:07")
    private LocalDateTime updatedAt;
}
