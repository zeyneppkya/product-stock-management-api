package com.zeynep.productapi.mapper;

import com.zeynep.productapi.dto.ProductRequest;
import com.zeynep.productapi.dto.ProductResponse;
import com.zeynep.productapi.model.Category;
import com.zeynep.productapi.model.Product;
import com.zeynep.productapi.model.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Product (model) ile DTO'lar arasinda cevirim yapar.
 * Cevaptaki "lowStock" alani burada, app.low-stock-threshold esigine gore hesaplanir.
 */
@Component
public class ProductMapper {

    private final int lowStockThreshold;

    public ProductMapper(@Value("${app.low-stock-threshold}") int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public Product toEntity(ProductRequest request, Category category, Supplier supplier) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .supplier(supplier)
                .price(request.getPrice())
                .stock(request.getStock())
                .build();
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .supplierId(product.getSupplier().getId())
                .supplierName(product.getSupplier().getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .lowStock(isLowStock(product))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    private boolean isLowStock(Product product) {
        return product.getStock() != null && product.getStock() < lowStockThreshold;
    }
}
