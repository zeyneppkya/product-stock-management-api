package com.zeynep.productapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tedarikci olusturma ve guncelleme isteklerinde kullanilir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tedarikci olusturma / guncelleme istegi")
public class SupplierRequest {

    @NotBlank(message = "Tedarikci adi bos olamaz")
    @Size(min = 2, max = 100, message = "Tedarikci adi 2 ile 100 karakter arasinda olmalidir")
    @Schema(description = "Firma adi", example = "Anadolu Teknoloji Dagitim A.S.")
    private String name;

    @Size(max = 100, message = "Yetkili adi en fazla 100 karakter olabilir")
    @Schema(description = "Firmadaki yetkili kisi (zorunlu degil)", example = "Ahmet Yilmaz")
    private String contactName;

    @Email(message = "Gecerli bir e-posta adresi giriniz")
    @Size(max = 100, message = "E-posta en fazla 100 karakter olabilir")
    @Schema(description = "E-posta adresi (zorunlu degil)", example = "satis@anadoluteknoloji.example")
    private String email;

    @Size(max = 20, message = "Telefon en fazla 20 karakter olabilir")
    @Schema(description = "Telefon numarasi (zorunlu degil)", example = "0212 555 01 01")
    private String phone;

    @Size(max = 255, message = "Adres en fazla 255 karakter olabilir")
    @Schema(description = "Adres (zorunlu degil)", example = "Maslak Mah. Buyukdere Cad. No:12 Sariyer/Istanbul")
    private String address;
}
