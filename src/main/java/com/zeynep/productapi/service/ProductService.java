package com.zeynep.productapi.service;

import com.zeynep.productapi.dto.ProductRequest;
import com.zeynep.productapi.dto.ProductResponse;
import com.zeynep.productapi.exception.CategoryNotFoundException;
import com.zeynep.productapi.exception.ProductNotFoundException;
import com.zeynep.productapi.exception.SupplierNotFoundException;
import com.zeynep.productapi.mapper.ProductMapper;
import com.zeynep.productapi.model.Category;
import com.zeynep.productapi.model.Product;
import com.zeynep.productapi.model.Supplier;
import com.zeynep.productapi.repository.CategoryRepository;
import com.zeynep.productapi.repository.ProductRepository;
import com.zeynep.productapi.repository.SupplierRepository;
import com.zeynep.productapi.specification.ProductSpecification;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Urun is mantigi.
 */
@Log4j2
@Service
public class ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper mapper;
    private final int lowStockThreshold;

    public ProductService(ProductRepository repository,
                          CategoryRepository categoryRepository,
                          SupplierRepository supplierRepository,
                          ProductMapper mapper,
                          @Value("${app.low-stock-threshold}") int lowStockThreshold) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
        this.lowStockThreshold = lowStockThreshold;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Urunler getiriliyor. sayfa={}, boyut={}, siralama={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<Product> products = repository.findAll(pageable);

        log.info("Toplam {} urunden {} tanesi getirildi.",
                products.getTotalElements(), products.getNumberOfElements());
        return products.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Urun getiriliyor. id={}", id);

        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        log.debug("Urun bulundu: {}", product.getName());
        return mapper.toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Yeni urun olusturuluyor. name={}", request.getName());

        Category category = findCategory(request.getCategoryId());
        Supplier supplier = findSupplier(request.getSupplierId());

        Product product = mapper.toEntity(request, category, supplier);
        Product saved = repository.save(product);

        log.info("Urun olusturuldu. id={}, name={}", saved.getId(), saved.getName());
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Urun guncelleniyor. id={}", id);

        Product existing = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        Category category = findCategory(request.getCategoryId());
        Supplier supplier = findSupplier(request.getSupplierId());

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setCategory(category);
        existing.setSupplier(supplier);
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());

        // Flush: updatedAt alani cevaba yeni degeriyle yansisin
        Product updated = repository.saveAndFlush(existing);

        log.info("Urun guncellendi. id={}, name={}", updated.getId(), updated.getName());
        return mapper.toResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Urun siliniyor. id={}", id);

        if (!repository.existsById(id)) {
            log.warn("Silinecek urun bulunamadi. id={}", id);
            throw new ProductNotFoundException(id);
        }

        repository.deleteById(id);
        log.info("Urun silindi. id={}", id);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category) {
        log.info("Kategoriye gore filtreleniyor. category={}", category);

        List<Product> products = repository.findByCategory_NameIgnoreCase(category);

        if (products.isEmpty()) {
            log.warn("'{}' kategorisinde hic urun yok.", category);
        } else {
            log.info("'{}' kategorisinde {} urun bulundu.", category, products.size());
        }

        return mapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        log.info("Az stoklu urunler getiriliyor. Esik deger: {}", lowStockThreshold);

        List<Product> products = repository.findByStockLessThan(lowStockThreshold);

        if (!products.isEmpty()) {
            log.warn("DIKKAT: {} adet urunun stogu {} adedin altinda!",
                    products.size(), lowStockThreshold);
        }

        return mapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name,
                                                Long categoryId,
                                                Long supplierId,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                Boolean lowStock,
                                                Pageable pageable) {
        log.info("Urun araniyor. name={}, categoryId={}, supplierId={}, minPrice={}, maxPrice={}, lowStock={}",
                name, categoryId, supplierId, minPrice, maxPrice, lowStock);

        Specification<Product> specification = ProductSpecification.filter(
                name, categoryId, supplierId, minPrice, maxPrice, lowStock, lowStockThreshold);

        Page<Product> products = repository.findAll(specification, pageable);

        log.info("Arama sonucu: {} urun bulundu.", products.getTotalElements());
        return products.map(mapper::toResponse);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));
    }
}
