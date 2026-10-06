package com.zeynep.productapi.specification;

import com.zeynep.productapi.model.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Urun aramasi icin dinamik sorgu kosullarini uretir.
 * Parametrelerin hepsi opsiyoneldir: null gelen parametre sorguya hic eklenmez.
 */
public final class ProductSpecification {

    private ProductSpecification() {
        // Sadece statik metot icerir, nesnesi olusturulmaz
    }

    /**
     * Verilen filtrelerin hepsini AND ile birlestirir.
     *
     * @param name              urun adinda gecen ifade (buyuk/kucuk harf duyarsiz)
     * @param categoryId        kategori ID
     * @param supplierId        tedarikci ID
     * @param minPrice          en dusuk fiyat (dahil)
     * @param maxPrice          en yuksek fiyat (dahil)
     * @param lowStock          true ise sadece stogu esigin altinda olanlar
     * @param lowStockThreshold "az stok" esigi
     */
    public static Specification<Product> filter(String name,
                                                Long categoryId,
                                                Long supplierId,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                Boolean lowStock,
                                                int lowStockThreshold) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                String pattern = "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (supplierId != null) {
                predicates.add(cb.equal(root.get("supplier").get("id"), supplierId));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (Boolean.TRUE.equals(lowStock)) {
                predicates.add(cb.lessThan(root.get("stock"), lowStockThreshold));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
