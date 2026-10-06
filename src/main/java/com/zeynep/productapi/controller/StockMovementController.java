package com.zeynep.productapi.controller;

import com.zeynep.productapi.dto.StockMovementRequest;
import com.zeynep.productapi.dto.StockMovementResponse;
import com.zeynep.productapi.exception.ErrorResponse;
import com.zeynep.productapi.model.MovementType;
import com.zeynep.productapi.service.StockMovementService;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Stok hareketi REST API.
 * Tum endpoint'ler /api/stock-movements altinda toplanir.
 * (Bir urunun hareket gecmisi ise ProductController'da: /api/products/{id}/stock-movements)
 */
@Log4j2
@RestController
@RequestMapping("/api/stock-movements")
@Tag(name = "Stok Hareketleri", description = "Stok giris / cikis islemleri ve hareket gecmisi")
public class StockMovementController {

    private final StockMovementService service;

    public StockMovementController(StockMovementService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Stok hareketi olustur",
            description = "IN: urunun stogunu miktar kadar artirir. OUT: miktar kadar azaltir. "
                    + "OUT isteginde stok yetersizse islem yapilmaz ve 400 doner.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hareket kaydedildi, stok guncellendi"),
            @ApiResponse(responseCode = "400", description = "Gecersiz veri ya da yetersiz stok",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Urun bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockMovementResponse> createMovement(
            @Valid @RequestBody StockMovementRequest request) {

        log.info("POST /api/stock-movements - productId={}, type={}, quantity={}",
                request.getProductId(), request.getType(), request.getQuantity());

        StockMovementResponse created = service.createMovement(request);

        // Hareketin tekil adresi yok; urunun hareket gecmisini Location basligina koy
        URI location = URI.create("/api/products/" + created.getProductId() + "/stock-movements");

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Stok hareketlerini listele",
            description = "Hareketleri sayfali olarak dondurur. productId ve type ile filtrelenebilir. "
                    + "Varsayilan siralama: en yeni hareket en ustte.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hareketler basariyla getirildi"),
            @ApiResponse(responseCode = "400", description = "Gecersiz parametre",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<StockMovementResponse>> getMovements(
            @Parameter(description = "Sadece bu urunun hareketleri", example = "1")
            @RequestParam(required = false) Long productId,
            @Parameter(description = "Sadece bu tipteki hareketler (IN / OUT)", example = "OUT")
            @RequestParam(required = false) MovementType type,
            @ParameterObject
            @SortDefault(sort = "movementDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/stock-movements - productId={}, type={}", productId, type);
        return ResponseEntity.ok(service.getMovements(productId, type, pageable));
    }
}
