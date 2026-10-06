package com.zeynep.productapi.mapper;

import com.zeynep.productapi.dto.SupplierRequest;
import com.zeynep.productapi.dto.SupplierResponse;
import com.zeynep.productapi.model.Supplier;
import org.springframework.stereotype.Component;

/**
 * Supplier (model) ile DTO'lar arasinda cevirim yapar.
 */
@Component
public class SupplierMapper {

    public Supplier toEntity(SupplierRequest request) {
        return Supplier.builder()
                .name(request.getName())
                .contactName(request.getContactName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
