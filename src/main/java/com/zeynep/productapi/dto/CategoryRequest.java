package com.zeynep.productapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kategori olusturma ve guncelleme isteklerinde kullanilir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kategori olusturma / guncelleme istegi")
public class CategoryRequest {

    @NotBlank(message = "Kategori adi bos olamaz")
    @Size(min = 2, max = 50, message = "Kategori adi 2 ile 50 karakter arasinda olmalidir")
    @Schema(description = "Kategori adi (benzersiz)", example = "Elektronik")
    private String name;

    @Size(max = 255, message = "Aciklama en fazla 255 karakter olabilir")
    @Schema(description = "Kategori aciklamasi (zorunlu degil)",
            example = "Bilgisayar cevre birimleri ve elektronik cihazlar")
    private String description;
}
