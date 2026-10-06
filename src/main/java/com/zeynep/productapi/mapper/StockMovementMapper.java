package com.zeynep.productapi.mapper;

import com.zeynep.productapi.dto.StockMovementResponse;
import com.zeynep.productapi.model.StockMovement;
import org.springframework.stereotype.Component;

/**
 * StockMovement (model) nesnesini cevap DTO'suna cevirir.
 * Request -> model cevirimi burada yoktur; hareket, is kurali geregi
 * StockMovementService icinde urunle birlikte olusturulur.
 */
@Component
public class StockMovementMapper {

    public StockMovementResponse toResponse(StockMovement movement) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .productId(movement.getProduct().getId())
                .productName(movement.getProduct().getName())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .note(movement.getNote())
                .movementDate(movement.getMovementDate())
                .stockAfter(movement.getStockAfter())
                .build();
    }
}
