package com.zeynep.productapi.exception;

/** Stok cikisinda (OUT) urunun mevcut stogu yetersizse firlatilir. */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int currentStock, int requestedQuantity) {
        super(String.format(
                "Yetersiz stok. Urun: %s, mevcut stok: %d, istenen miktar: %d",
                productName, currentStock, requestedQuantity));
    }
}
