package com.zeynep.productapi.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Tum hatalarin kullaniciya dondugu standart cevap formati.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standart hata cevabi")
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Hatanin olustugu an", example = "2026-08-17 14:32:07")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP durum kodu", example = "404")
    private int status;

    @Schema(description = "HTTP durum aciklamasi", example = "Not Found")
    private String error;

    @Schema(description = "Hata mesaji", example = "Urun bulunamadi. ID: 999")
    private String message;

    @Schema(description = "Istegin yapildigi adres", example = "/api/products/999")
    private String path;

    /** Yalnizca dogrulama hatalarinda dolar; diger hatalarda null kalir ve JSON'a yazilmaz. */
    @Schema(description = "Alan bazli dogrulama hatalari")
    private Map<String, String> validationErrors;
}
