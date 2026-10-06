package com.zeynep.productapi.controller;

import com.zeynep.productapi.dto.SupplierRequest;
import com.zeynep.productapi.dto.SupplierResponse;
import com.zeynep.productapi.exception.ErrorResponse;
import com.zeynep.productapi.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Tedarikci REST API.
 * Tum endpoint'ler /api/suppliers altinda toplanir.
 */
@Log4j2
@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Tedarikciler", description = "Tedarikci yonetimi islemleri (CRUD)")
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Tum tedarikcileri getir",
            description = "Tedarikcileri sayfali olarak dondurur. Ornek: ?page=0&size=20&sort=name,asc")
    @ApiResponse(responseCode = "200", description = "Tedarikciler basariyla getirildi")
    public ResponseEntity<Page<SupplierResponse>> getAllSuppliers(
            @ParameterObject @SortDefault(sort = "id") Pageable pageable) {

        log.info("GET /api/suppliers - page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(service.getAllSuppliers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile tedarikci getir",
            description = "Verilen ID'ye sahip tedarikciyi dondurur.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tedarikci bulundu"),
            @ApiResponse(responseCode = "404", description = "Tedarikci bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> getSupplierById(
            @Parameter(description = "Tedarikci ID", example = "1")
            @PathVariable Long id) {

        log.info("GET /api/suppliers/{}", id);
        return ResponseEntity.ok(service.getSupplierById(id));
    }

    @PostMapping
    @Operation(summary = "Yeni tedarikci olustur",
            description = "Gonderilen bilgilerle yeni bir tedarikci kaydeder. ID otomatik atanir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tedarikci olusturuldu"),
            @ApiResponse(responseCode = "400", description = "Gecersiz veri",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody SupplierRequest request) {

        log.info("POST /api/suppliers - name={}", request.getName());

        SupplierResponse created = service.createSupplier(request);

        URI location = URI.create("/api/suppliers/" + created.getId());

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Tedarikci guncelle",
            description = "Verilen ID'ye sahip tedarikcinin tum bilgilerini gunceller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tedarikci guncellendi"),
            @ApiResponse(responseCode = "400", description = "Gecersiz veri",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tedarikci bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> updateSupplier(
            @Parameter(description = "Guncellenecek tedarikcinin ID'si", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {

        log.info("PUT /api/suppliers/{}", id);
        return ResponseEntity.ok(service.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Tedarikci sil",
            description = "Verilen ID'ye sahip tedarikciyi siler. Urunu olan tedarikci silinemez.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tedarikci silindi (govde bos doner)"),
            @ApiResponse(responseCode = "404", description = "Tedarikci bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Tedarikciye bagli urunler oldugu icin silinemedi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "Silinecek tedarikcinin ID'si", example = "1")
            @PathVariable Long id) {

        log.info("DELETE /api/suppliers/{}", id);

        service.deleteSupplier(id);

        return ResponseEntity.noContent().build();
    }
}
