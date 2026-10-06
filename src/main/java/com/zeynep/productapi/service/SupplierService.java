package com.zeynep.productapi.service;

import com.zeynep.productapi.dto.SupplierRequest;
import com.zeynep.productapi.dto.SupplierResponse;
import com.zeynep.productapi.exception.SupplierNotFoundException;
import com.zeynep.productapi.mapper.SupplierMapper;
import com.zeynep.productapi.model.Supplier;
import com.zeynep.productapi.repository.SupplierRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tedarikci is mantigi.
 */
@Log4j2
@Service
public class SupplierService {

    private final SupplierRepository repository;
    private final SupplierMapper mapper;

    public SupplierService(SupplierRepository repository, SupplierMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAllSuppliers(Pageable pageable) {
        log.info("Tedarikciler getiriliyor. sayfa={}, boyut={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Supplier> suppliers = repository.findAll(pageable);

        log.info("Toplam {} tedarikci bulundu.", suppliers.getTotalElements());
        return suppliers.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        log.info("Tedarikci getiriliyor. id={}", id);

        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        log.debug("Tedarikci bulundu: {}", supplier.getName());
        return mapper.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        log.info("Yeni tedarikci olusturuluyor. name={}", request.getName());

        Supplier saved = repository.save(mapper.toEntity(request));

        log.info("Tedarikci olusturuldu. id={}, name={}", saved.getId(), saved.getName());
        return mapper.toResponse(saved);
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        log.info("Tedarikci guncelleniyor. id={}", id);

        Supplier existing = repository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        existing.setName(request.getName());
        existing.setContactName(request.getContactName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());

        // Flush: updatedAt alani cevaba yeni degeriyle yansisin
        Supplier updated = repository.saveAndFlush(existing);

        log.info("Tedarikci guncellendi. id={}, name={}", updated.getId(), updated.getName());
        return mapper.toResponse(updated);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        log.info("Tedarikci siliniyor. id={}", id);

        if (!repository.existsById(id)) {
            log.warn("Silinecek tedarikci bulunamadi. id={}", id);
            throw new SupplierNotFoundException(id);
        }

        repository.deleteById(id);
        log.info("Tedarikci silindi. id={}", id);
    }
}
