package com.zeynep.productapi.exception;

/** Aranan urun bulunamadiginda firlatilir. */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Urun bulunamadi. ID: " + id);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}