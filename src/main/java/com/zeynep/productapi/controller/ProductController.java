package com.zeynep.productapi.controller;

import com.zeynep.productapi.dto.ProductRequest;
import com.zeynep.productapi.dto.ProductResponse;
import com.zeynep.productapi.dto.StockMovementResponse;
import com.zeynep.productapi.exception.ErrorResponse;
import com.zeynep.productapi.service.ProductService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

/**
 * Urun REST API.
 * Tum endpoint'ler /api/products altinda toplanir.
 */
@Log4j2
@RestController
@RequestMapping("/api/products")
@Tag(name = "Urunler", description = "Urun yonetimi islemleri (CRUD + arama / filtreleme)")
public class ProductController {

    private final ProductService service;
    private final StockMovementService stockMovementService;

    public ProductController(ProductService service, StockMovementService stockMovementService) {
        this.service = service;
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    @Operation(summary = "Tum urunleri getir",
            description = "Urunleri sayfali olarak dondurur. Ornek: ?page=0&size=20&sort=name,asc")
    @ApiResponse(responseCode = "200", description = "Urunler basariyla getirildi")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @ParameterObject @SortDefault(sort = "id") Pageable pageable) {

        log.info("GET /api/products - page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(service.getAllProducts(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Urun ara / filtrele",
            description = "Tum parametreler opsiyoneldir; verilenler AND ile birlestirilir. Sonuc sayfalidir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arama basarili"),
            @ApiResponse(responseCode = "400", description = "Gecersiz parametre",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @Parameter(description = "Urun adinda gecen ifade (buyuk/kucuk harf duyarsiz)", example = "klavye")
            @RequestParam(required = false) String name,
            @Parameter(description = "Kategori ID", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Tedarikci ID", example = "1")
            @RequestParam(required = false) Long supplierId,
            @Parameter(description = "En dusuk fiyat (dahil)", example = "100")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "En yuksek fiyat (dahil)", example = "5000")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "true ise sadece stogu esigin altinda olan urunler", example = "true")
            @RequestParam(required = false) Boolean lowStock,
            @ParameterObject @SortDefault(sort = "id") Pageable pageable) {

        log.info("GET /api/products/search");
        return ResponseEntity.ok(service.searchProducts(
                name, categoryId, supplierId, minPrice, maxPrice, lowStock, pageable));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Az stoklu urunleri getir",
            description = "Stogu, application.properties'teki esik degerin altinda olan urunleri dondurur.")
    @ApiResponse(responseCode = "200", description = "Az stoklu urunler getirildi")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {
        log.info("GET /api/products/low-stock");
        return ResponseEntity.ok(service.getLowStockProducts());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Kategori adina gore filtrele",
            description = "Belirtilen kategorideki urunleri dondurur. Buyuk/kucuk harf farki gozetmez.")
    @ApiResponse(responseCode = "200", description = "Filtreleme basarili")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @Parameter(description = "Kategori adi", example = "Elektronik")
            @PathVariable String category) {

        log.info("GET /api/products/category/{}", category);
        return ResponseEntity.ok(service.getProductsByCategory(category));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile urun getir",
            description = "Verilen ID'ye sahip urunu dondurur.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Urun bulundu"),
            @ApiResponse(responseCode = "404", description = "Urun bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Urun ID", example = "1")
            @PathVariable Long id) {

        log.info("GET /api/products/{}", id);
        return ResponseEntity.ok(service.getProductById(id));
    }

    @GetMapping("/{id}/stock-movements")
    @Operation(summary = "Urunun stok hareket gecmisini getir",
            description = "Verilen urune ait stok giris / cikis hareketlerini sayfali olarak dondurur. "
                    + "Varsayilan siralama: en yeni hareket en ustte.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hareket gecmisi getirildi"),
            @ApiResponse(responseCode = "404", description = "Urun bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<StockMovementResponse>> getProductStockMovements(
            @Parameter(description = "Urun ID", example = "1")
            @PathVariable Long id,
            @ParameterObject
            @SortDefault(sort = "movementDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/products/{}/stock-movements", id);
        return ResponseEntity.ok(stockMovementService.getMovementsByProduct(id, pageable));
    }

    @PostMapping
    @Operation(summary = "Yeni urun olustur",
            description = "Gonderilen bilgilerle yeni bir urun kaydeder. ID otomatik atanir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Urun olusturuldu"),
            @ApiResponse(responseCode = "400", description = "Gecersiz veri",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kategori ya da tedarikci bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {

        log.info("POST /api/products - name={}", request.getName());

        ProductResponse created = service.createProduct(request);

        URI location = URI.create("/api/products/" + created.getId());

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Urun guncelle",
            description = "Verilen ID'ye sahip urunun tum bilgilerini gunceller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Urun guncellendi"),
            @ApiResponse(responseCode = "400", description = "Gecersiz veri",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Urun, kategori ya da tedarikci bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Guncellenecek urunun ID'si", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        log.info("PUT /api/products/{}", id);
        return ResponseEntity.ok(service.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Urun sil",
            description = "Verilen ID'ye sahip urunu siler. Stok hareketi olan urun silinemez.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Urun silindi (govde bos doner)"),
            @ApiResponse(responseCode = "404", description = "Urun bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Urunun stok hareketi oldugu icin silinemedi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Silinecek urunun ID'si", example = "1")
            @PathVariable Long id) {

        log.info("DELETE /api/products/{}", id);

        service.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}
