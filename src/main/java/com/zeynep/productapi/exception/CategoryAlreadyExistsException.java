package com.zeynep.productapi.exception;

/** Ayni isimde bir kategori zaten varken yenisi eklenmek istendiginde firlatilir. */
public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException(String name) {
        super("Bu isimde bir kategori zaten var: " + name);
    }
}
