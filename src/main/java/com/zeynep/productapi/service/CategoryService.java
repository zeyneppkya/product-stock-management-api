package com.zeynep.productapi.service;

import com.zeynep.productapi.dto.CategoryRequest;
import com.zeynep.productapi.dto.CategoryResponse;
import com.zeynep.productapi.exception.CategoryAlreadyExistsException;
import com.zeynep.productapi.exception.CategoryNotFoundException;
import com.zeynep.productapi.mapper.CategoryMapper;
import com.zeynep.productapi.model.Category;
import com.zeynep.productapi.repository.CategoryRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kategori is mantigi.
 */
@Log4j2
@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository repository, CategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        log.info("Kategoriler getiriliyor. sayfa={}, boyut={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Category> categories = repository.findAll(pageable);

        log.info("Toplam {} kategori bulundu.", categories.getTotalElements());
        return categories.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        log.info("Kategori getiriliyor. id={}", id);

        Category category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        log.debug("Kategori bulundu: {}", category.getName());
        return mapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Yeni kategori olusturuluyor. name={}", request.getName());

        if (repository.findByNameIgnoreCase(request.getName()).isPresent()) {
            log.warn("Ayni isimde kategori zaten var. name={}", request.getName());
            throw new CategoryAlreadyExistsException(request.getName());
        }

        Category saved = repository.save(mapper.toEntity(request));

        log.info("Kategori olusturuldu. id={}, name={}", saved.getId(), saved.getName());
        return mapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Kategori guncelleniyor. id={}", id);

        Category existing = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // Yeni isim BASKA bir kategoride kullaniliyorsa izin verme
        repository.findByNameIgnoreCase(request.getName())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    log.warn("Ayni isimde baska bir kategori var. name={}", request.getName());
                    throw new CategoryAlreadyExistsException(request.getName());
                });

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());

        // Flush: updatedAt alani cevaba yeni degeriyle yansisin
        Category updated = repository.saveAndFlush(existing);

        log.info("Kategori guncellendi. id={}, name={}", updated.getId(), updated.getName());
        return mapper.toResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Kategori siliniyor. id={}", id);

        if (!repository.existsById(id)) {
            log.warn("Silinecek kategori bulunamadi. id={}", id);
            throw new CategoryNotFoundException(id);
        }

        repository.deleteById(id);
        log.info("Kategori silindi. id={}", id);
    }
}
