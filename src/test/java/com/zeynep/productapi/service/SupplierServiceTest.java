package com.zeynep.productapi.service;

import com.zeynep.productapi.dto.SupplierRequest;
import com.zeynep.productapi.dto.SupplierResponse;
import com.zeynep.productapi.exception.SupplierNotFoundException;
import com.zeynep.productapi.mapper.SupplierMapper;
import com.zeynep.productapi.model.Supplier;
import com.zeynep.productapi.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SupplierService birim testleri.
 */
@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository repository;

    private SupplierService service;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        service = new SupplierService(repository, new SupplierMapper());

        supplier = Supplier.builder()
                .id(1L)
                .name("Anadolu Teknoloji Dagitim A.S.")
                .contactName("Ahmet Yilmaz")
                .email("satis@anadoluteknoloji.example")
                .phone("0212 555 01 01")
                .address("Sariyer/Istanbul")
                .build();
    }

    private SupplierRequest sampleRequest() {
        return SupplierRequest.builder()
                .name("Toros Ag Sistemleri Ltd. Sti.")
                .contactName("Burak Sahin")
                .email("destek@torosag.example")
                .phone("0322 555 06 06")
                .address("Seyhan/Adana")
                .build();
    }

    @Test
    @DisplayName("Listeleme: sayfali sonuc cevaba cevrilir")
    void getAllSuppliers_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(supplier), pageable, 1));

        Page<SupplierResponse> page = service.getAllSuppliers(pageable);

        assertThat(page.getContent()).extracting(SupplierResponse::getName)
                .containsExactly("Anadolu Teknoloji Dagitim A.S.");
    }

    @Test
    @DisplayName("ID ile bulma: tedarikci varsa cevap doner")
    void getSupplierById_returnsSupplier_whenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        SupplierResponse response = service.getSupplierById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getContactName()).isEqualTo("Ahmet Yilmaz");
        assertThat(response.getEmail()).isEqualTo("satis@anadoluteknoloji.example");
    }

    @Test
    @DisplayName("ID ile bulma: tedarikci yoksa SupplierNotFoundException firlatilir")
    void getSupplierById_throws_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSupplierById(99L))
                .isInstanceOf(SupplierNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Olusturma: tedarikci kaydedilir")
    void createSupplier_savesAndReturnsSupplier() {
        when(repository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        SupplierResponse response = service.createSupplier(sampleRequest());

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Toros Ag Sistemleri Ltd. Sti.");
        assertThat(response.getPhone()).isEqualTo("0322 555 06 06");
    }

    @Test
    @DisplayName("Guncelleme: mevcut tedarikcinin alanlari degisir")
    void updateSupplier_updatesFields() {
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.saveAndFlush(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierResponse response = service.updateSupplier(1L, sampleRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Toros Ag Sistemleri Ltd. Sti.");
        assertThat(response.getContactName()).isEqualTo("Burak Sahin");
        assertThat(response.getAddress()).isEqualTo("Seyhan/Adana");
    }

    @Test
    @DisplayName("Guncelleme: tedarikci yoksa SupplierNotFoundException firlatilir")
    void updateSupplier_throws_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSupplier(99L, sampleRequest()))
                .isInstanceOf(SupplierNotFoundException.class);

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Silme: tedarikci varsa silinir")
    void deleteSupplier_deletes_whenFound() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteSupplier(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Silme: tedarikci yoksa SupplierNotFoundException firlatilir, silme yapilmaz")
    void deleteSupplier_throws_whenNotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteSupplier(99L))
                .isInstanceOf(SupplierNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
