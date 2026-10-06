package com.zeynep.productapi.mapper;

import com.zeynep.productapi.dto.CategoryRequest;
import com.zeynep.productapi.dto.CategoryResponse;
import com.zeynep.productapi.model.Category;
import org.springframework.stereotype.Component;

/**
 * Category (model) ile DTO'lar arasinda cevirim yapar.
 */
@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
