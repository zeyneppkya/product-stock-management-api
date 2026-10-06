package com.zeynep.productapi.controller;

import com.zeynep.productapi.service.ExcelReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Rapor REST API.
 * Tum endpoint'ler /api/reports altinda toplanir ve indirilebilir dosya dondurur.
 */
@Log4j2
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Raporlar", description = "Excel (.xlsx) raporlari")
public class ReportController {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /** Dosya adina eklenen tarih damgasi. Ornek: 20261005-1432 */
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final ExcelReportService service;

    public ReportController(ExcelReportService service) {
        this.service = service;
    }

    @GetMapping("/products/excel")
    @Operation(summary = "Urun listesini Excel olarak indir",
            description = "Tum urunleri kategori, tedarikci, fiyat ve stok bilgileriyle .xlsx dosyasi olarak dondurur.")
    @ApiResponse(responseCode = "200", description = "Rapor olusturuldu",
            content = @Content(mediaType = XLSX_CONTENT_TYPE))
    public ResponseEntity<byte[]> downloadProductReport() {
        log.info("GET /api/reports/products/excel");
        return buildExcelResponse(service.generateProductReport(), "urunler");
    }

    @GetMapping("/stock-movements/excel")
    @Operation(summary = "Stok hareketlerini Excel olarak indir",
            description = "Tum stok hareketlerini (en yeni en ustte) .xlsx dosyasi olarak dondurur.")
    @ApiResponse(responseCode = "200", description = "Rapor olusturuldu",
            content = @Content(mediaType = XLSX_CONTENT_TYPE))
    public ResponseEntity<byte[]> downloadStockMovementReport() {
        log.info("GET /api/reports/stock-movements/excel");
        return buildExcelResponse(service.generateStockMovementReport(), "stok-hareketleri");
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] content, String baseName) {
        String fileName = baseName + "-" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".xlsx";

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE))
                .contentLength(content.length)
                .body(content);
    }
}
