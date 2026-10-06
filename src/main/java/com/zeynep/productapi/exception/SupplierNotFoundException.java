package com.zeynep.productapi.exception;

/** Aranan tedarikci bulunamadiginda firlatilir. */
public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(Long id) {
        super("Tedarikci bulunamadi. ID: " + id);
    }

    public SupplierNotFoundException(String message) {
        super(message);
    }
}
