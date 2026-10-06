package com.zeynep.productapi.exception;

/** Aranan kategori bulunamadiginda firlatilir. */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super("Kategori bulunamadi. ID: " + id);
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
