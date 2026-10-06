package com.zeynep.productapi.service;

import com.zeynep.productapi.dto.StockMovementRequest;
import com.zeynep.productapi.dto.StockMovementResponse;
import com.zeynep.productapi.exception.InsufficientStockException;
import com.zeynep.productapi.exception.ProductNotFoundException;
import com.zeynep.productapi.mapper.StockMovementMapper;
import com.zeynep.productapi.model.MovementType;
import com.zeynep.productapi.model.Product;
import com.zeynep.productapi.model.StockMovement;
import com.zeynep.productapi.repository.ProductRepository;
import com.zeynep.productapi.repository.StockMovementRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stok hareketi is mantigi.
 * Urun stogu SADECE buradaki kurallarla artar / azalir.
 */
@Log4j2
@Service
public class StockMovementService {

    private final StockMovementRepository repository;
    private final ProductRepository productRepository;
    private final StockMovementMapper mapper;

    public StockMovementService(StockMovementRepository repository,
                                ProductRepository productRepository,
                                StockMovementMapper mapper) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    // IN  -> stok quantity kadar ARTAR
    // OUT -> stok quantity kadar AZALIR, stok yetersizse hata firlatilir
    // Hareket kaydi ve urun stogu AYNI transaction icinde guncellenir:
    // biri basarisiz olursa digeri de geri alinir.
    @Transactional
    public StockMovementResponse createMovement(StockMovementRequest request) {
        log.info("Stok hareketi olusturuluyor. productId={}, type={}, quantity={}",
                request.getProductId(), request.getType(), request.getQuantity());

        // Urunu kilitleyerek getir: es zamanli hareketler stogu bozmasin
        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        int currentStock = product.getStock();
        int quantity = request.getQuantity();
        int newStock;

        if (request.getType() == MovementType.IN) {
            newStock = currentStock + quantity;
        } else {
            if (currentStock < quantity) {
                log.warn("Yetersiz stok. productId={}, mevcut={}, istenen={}",
                        product.getId(), currentStock, quantity);
                throw new InsufficientStockException(product.getName(), currentStock, quantity);
            }
            newStock = currentStock - quantity;
        }

        product.setStock(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(request.getType())
                .quantity(quantity)
                .note(request.getNote())
                .stockAfter(newStock)
                .build();

        StockMovement saved = repository.save(movement);

        log.info("Stok hareketi kaydedildi. id={}, urun={}, stok: {} -> {}",
                saved.getId(), product.getName(), currentStock, newStock);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getMovements(Long productId, MovementType type, Pageable pageable) {
        log.info("Stok hareketleri getiriliyor. productId={}, type={}, sayfa={}, boyut={}",
                productId, type, pageable.getPageNumber(), pageable.getPageSize());

        Page<StockMovement> movements;

        if (productId != null && type != null) {
            movements = repository.findByProduct_IdAndType(productId, type, pageable);
        } else if (productId != null) {
            movements = repository.findByProduct_Id(productId, pageable);
        } else if (type != null) {
            movements = repository.findByType(type, pageable);
        } else {
            movements = repository.findAll(pageable);
        }

        log.info("Toplam {} stok hareketi bulundu.", movements.getTotalElements());
        return movements.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getMovementsByProduct(Long productId, Pageable pageable) {
        log.info("Urunun hareket gecmisi getiriliyor. productId={}", productId);

        // Olmayan bir urun icin bos liste degil 404 donsun
        if (!productRepository.existsById(productId)) {
            log.warn("Hareket gecmisi istenen urun bulunamadi. productId={}", productId);
            throw new ProductNotFoundException(productId);
        }

        Page<StockMovement> movements = repository.findByProduct_Id(productId, pageable);

        log.info("Urunun {} stok hareketi bulundu. productId={}", movements.getTotalElements(), productId);
        return movements.map(mapper::toResponse);
    }
}
