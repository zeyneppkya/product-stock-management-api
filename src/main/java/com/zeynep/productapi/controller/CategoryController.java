package com.zeynep.productapi.controller;

import com.zeynep.productapi.dto.CategoryRequest;
import com.zeynep.productapi.dto.CategoryResponse;
import com.zeynep.productapi.exception.ErrorResponse;
import com.zeynep.productapi.service.CategoryService;
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
 * Kategori REST API.
 * Tum endpoint'ler /api/categories altinda toplanir.
 */
@Log4j2
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Kategoriler", description = "Kategori yonetimi islemleri (CRUD)")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Tum kategorileri getir",
            description = "Kategorileri sayfali olarak dondurur. Ornek: ?page=0&size=20&sort=name,asc")
    @ApiResponse(responseCode = "200", description = "Kategoriler basariyla getirildi")
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @ParameterObject @SortDefault(sort = "id") Pageable pageable) {

        log.info("GET /api/categories - page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(service.getAllCategories(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile kategori getir",
            description = "Verilen ID'ye sahip kategoriyi dondurur.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategori bulundu"),
            @ApiResponse(responseCode = "404", description = "Kategori bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Kategori ID", example = "1")
            @PathVariable Long id) {

        log.info("GET /api/categories/{}", id);
        return ResponseEntity.ok(service.getCategoryById(id));
    }

    @PostMapping
    @Operation(summary = "Yeni kategori olustur",
            description = "Gonderilen bilgilerle yeni bir kategori kaydeder. Kategori adi benzersiz olmalidir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kategori olusturuldu"),
            @ApiResponse(responseCode = "400", description = "Gecersiz veri",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ayni isimde kategori zaten var",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        log.info("POST /api/categories - name={}", request.getName());

        CategoryResponse created = service.createCategory(request);

        URI location = URI.create("/api/categories/" + created.getId());

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Kategori guncelle",
            description = "Verilen ID'ye sahip kategorinin tum bilgilerini gunceller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategori guncellendi"),
            @ApiResponse(responseCode = "400", description = "Gecersiz veri",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kategori bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ayni isimde baska bir kategori var",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Guncellenecek kategorinin ID'si", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        log.info("PUT /api/categories/{}", id);
        return ResponseEntity.ok(service.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Kategori sil",
            description = "Verilen ID'ye sahip kategoriyi siler. Urunu olan kategori silinemez.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Kategori silindi (govde bos doner)"),
            @ApiResponse(responseCode = "404", description = "Kategori bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Kategoriye bagli urunler oldugu icin silinemedi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Silinecek kategorinin ID'si", example = "1")
            @PathVariable Long id) {

        log.info("DELETE /api/categories/{}", id);

        service.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }
}
