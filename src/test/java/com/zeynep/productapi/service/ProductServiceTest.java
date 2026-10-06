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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProductService birim testleri.
 * Repository'ler Mockito ile taklit edilir; veritabanina gidilmez.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final int LOW_STOCK_THRESHOLD = 10;

    @Mock
    private ProductRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    private ProductService service;

    private Category category;
    private Supplier supplier;
    private Product product;

    @BeforeEach
    void setUp() {
        // Mapper'da taklit edilecek bir bagimlilik yok, gercegi kullanilir
        ProductMapper mapper = new ProductMapper(LOW_STOCK_THRESHOLD);
        service = new ProductService(repository, categoryRepository, supplierRepository,
                mapper, LOW_STOCK_THRESHOLD);

        category = Category.builder().id(1L).name("Elektronik").build();
        supplier = Supplier.builder().id(2L).name("Anadolu Teknoloji Dagitim A.S.").build();
        product = Product.builder()
                .id(10L)
                .name("Mekanik Klavye")
                .description("RGB isikli, mavi switch")
                .category(category)
                .supplier(supplier)
                .price(new BigDecimal("1450.00"))
                .stock(25)
                .build();
    }

    private ProductRequest sampleRequest() {
        return ProductRequest.builder()
                .name("Kablosuz Mouse")
                .description("2.4 GHz, sessiz tiklama")
                .categoryId(1L)
                .supplierId(2L)
                .price(new BigDecimal("380.50"))
                .stock(8)
                .build();
    }

    @Test
    @DisplayName("ID ile bulma: urun varsa cevap doner")
    void getProductById_returnsProduct_whenFound() {
        when(repository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponse response = service.getProductById(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Mekanik Klavye");
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo("Elektronik");
        assertThat(response.getSupplierId()).isEqualTo(2L);
        assertThat(response.isLowStock()).isFalse();
    }

    @Test
    @DisplayName("ID ile bulma: urun yoksa ProductNotFoundException firlatilir")
    void getProductById_throws_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Listeleme: sayfali sonuc cevaba cevrilir")
    void getAllProducts_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        Page<ProductResponse> page = service.getAllProducts(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).extracting(ProductResponse::getName).containsExactly("Mekanik Klavye");
    }

    @Test
    @DisplayName("Olusturma: urun kategori ve tedarikcisiyle kaydedilir")
    void createProduct_savesAndReturnsProduct() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(supplier));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        ProductResponse response = service.createProduct(sampleRequest());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isSameAs(category);
        assertThat(captor.getValue().getSupplier()).isSameAs(supplier);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getName()).isEqualTo("Kablosuz Mouse");
        assertThat(response.getStock()).isEqualTo(8);
        assertThat(response.isLowStock()).isTrue();   // 8 < 10
    }

    @Test
    @DisplayName("Olusturma: kategori yoksa CategoryNotFoundException firlatilir, kayit yapilmaz")
    void createProduct_throws_whenCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createProduct(sampleRequest()))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Olusturma: tedarikci yoksa SupplierNotFoundException firlatilir, kayit yapilmaz")
    void createProduct_throws_whenSupplierNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createProduct(sampleRequest()))
                .isInstanceOf(SupplierNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Guncelleme: mevcut urunun alanlari istekteki degerlerle degisir")
    void updateProduct_updatesFields() {
        Category newCategory = Category.builder().id(3L).name("Aksesuar").build();
        ProductRequest request = sampleRequest();
        request.setCategoryId(3L);

        when(repository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(newCategory));
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(supplier));
        when(repository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = service.updateProduct(10L, request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Kablosuz Mouse");
        assertThat(response.getCategoryName()).isEqualTo("Aksesuar");
        assertThat(response.getPrice()).isEqualByComparingTo("380.50");
        assertThat(response.getStock()).isEqualTo(8);
        verify(repository).saveAndFlush(product);
    }

    @Test
    @DisplayName("Guncelleme: urun yoksa ProductNotFoundException firlatilir")
    void updateProduct_throws_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProduct(99L, sampleRequest()))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Silme: urun varsa silinir")
    void deleteProduct_deletes_whenFound() {
        when(repository.existsById(10L)).thenReturn(true);

        service.deleteProduct(10L);

        verify(repository).deleteById(10L);
    }

    @Test
    @DisplayName("Silme: urun yoksa ProductNotFoundException firlatilir, silme yapilmaz")
    void deleteProduct_throws_whenNotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
