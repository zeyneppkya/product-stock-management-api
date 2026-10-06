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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * StockMovementService birim testleri.
 * Stok is kurallarini dogrular: IN artirir, OUT azaltir, yetersiz stokta hata firlatilir.
 */
@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    private static final Long PRODUCT_ID = 1L;
    private static final int INITIAL_STOCK = 10;

    @Mock
    private StockMovementRepository repository;

    @Mock
    private ProductRepository productRepository;

    private StockMovementService service;

    private Product product;

    @BeforeEach
    void setUp() {
        service = new StockMovementService(repository, productRepository, new StockMovementMapper());

        product = Product.builder()
                .id(PRODUCT_ID)
                .name("Mekanik Klavye")
                .price(new BigDecimal("1450.00"))
                .stock(INITIAL_STOCK)
                .build();
    }

    private StockMovementRequest request(MovementType type, int quantity) {
        return StockMovementRequest.builder()
                .productId(PRODUCT_ID)
                .type(type)
                .quantity(quantity)
                .note("Test hareketi")
                .build();
    }

    /** Urun bulunur ve hareket kaydi basarili olur. */
    private void givenProductExistsAndMovementIsSaved() {
        when(productRepository.findByIdForUpdate(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(repository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });
    }

    @Test
    @DisplayName("IN hareketi urunun stogunu miktar kadar artirir")
    void createMovement_in_increasesStock() {
        givenProductExistsAndMovementIsSaved();

        StockMovementResponse response = service.createMovement(request(MovementType.IN, 5));

        assertThat(product.getStock()).isEqualTo(15);
        verify(productRepository).save(product);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getProductName()).isEqualTo("Mekanik Klavye");
        assertThat(response.getType()).isEqualTo(MovementType.IN);
        assertThat(response.getQuantity()).isEqualTo(5);
        assertThat(response.getStockAfter()).isEqualTo(15);
    }

    @Test
    @DisplayName("OUT hareketi urunun stogunu miktar kadar azaltir")
    void createMovement_out_decreasesStock() {
        givenProductExistsAndMovementIsSaved();

        StockMovementResponse response = service.createMovement(request(MovementType.OUT, 4));

        assertThat(product.getStock()).isEqualTo(6);
        verify(productRepository).save(product);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(MovementType.OUT);
        assertThat(captor.getValue().getQuantity()).isEqualTo(4);
        assertThat(captor.getValue().getStockAfter()).isEqualTo(6);

        assertThat(response.getStockAfter()).isEqualTo(6);
    }

    @Test
    @DisplayName("OUT hareketi stogun tamamini cikarabilir (stok sifira iner)")
    void createMovement_out_allowsExactStock() {
        givenProductExistsAndMovementIsSaved();

        StockMovementResponse response = service.createMovement(request(MovementType.OUT, INITIAL_STOCK));

        assertThat(product.getStock()).isZero();
        assertThat(response.getStockAfter()).isZero();
    }

    @Test
    @DisplayName("Yetersiz stokta InsufficientStockException firlatilir, stok ve hareket degismez")
    void createMovement_out_throws_whenStockInsufficient() {
        when(productRepository.findByIdForUpdate(PRODUCT_ID)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.createMovement(request(MovementType.OUT, 11)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("mevcut stok: 10")
                .hasMessageContaining("istenen miktar: 11");

        assertThat(product.getStock()).isEqualTo(INITIAL_STOCK);
        verify(productRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Urun yoksa ProductNotFoundException firlatilir, hareket kaydedilmez")
    void createMovement_throws_whenProductNotFound() {
        when(productRepository.findByIdForUpdate(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createMovement(request(MovementType.IN, 5)))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Hareket gecmisi: urun yoksa ProductNotFoundException firlatilir")
    void getMovementsByProduct_throws_whenProductNotFound() {
        Pageable pageable = PageRequest.of(0, 20);
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getMovementsByProduct(99L, pageable))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository, never()).findByProduct_Id(any(), any());
    }
}
