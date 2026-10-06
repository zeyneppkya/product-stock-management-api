package com.zeynep.productapi.service;

import com.zeynep.productapi.dto.CategoryRequest;
import com.zeynep.productapi.dto.CategoryResponse;
import com.zeynep.productapi.exception.CategoryAlreadyExistsException;
import com.zeynep.productapi.exception.CategoryNotFoundException;
import com.zeynep.productapi.mapper.CategoryMapper;
import com.zeynep.productapi.model.Category;
import com.zeynep.productapi.repository.CategoryRepository;
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
 * CategoryService birim testleri.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    private CategoryService service;

    private Category category;

    @BeforeEach
    void setUp() {
        service = new CategoryService(repository, new CategoryMapper());

        category = Category.builder()
                .id(1L)
                .name("Elektronik")
                .description("Elektronik cihazlar")
                .build();
    }

    private CategoryRequest request(String name) {
        return CategoryRequest.builder().name(name).description("Aciklama").build();
    }

    @Test
    @DisplayName("Listeleme: sayfali sonuc cevaba cevrilir")
    void getAllCategories_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category), pageable, 1));

        Page<CategoryResponse> page = service.getAllCategories(pageable);

        assertThat(page.getContent()).extracting(CategoryResponse::getName).containsExactly("Elektronik");
    }

    @Test
    @DisplayName("ID ile bulma: kategori varsa cevap doner")
    void getCategoryById_returnsCategory_whenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse response = service.getCategoryById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Elektronik");
    }

    @Test
    @DisplayName("ID ile bulma: kategori yoksa CategoryNotFoundException firlatilir")
    void getCategoryById_throws_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCategoryById(99L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Olusturma: isim kullanilmiyorsa kategori kaydedilir")
    void createCategory_saves_whenNameIsFree() {
        when(repository.findByNameIgnoreCase("Mobilya")).thenReturn(Optional.empty());
        when(repository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        CategoryResponse response = service.createCategory(request("Mobilya"));

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Mobilya");
    }

    @Test
    @DisplayName("Olusturma: ayni isim varsa CategoryAlreadyExistsException firlatilir, kayit yapilmaz")
    void createCategory_throws_whenNameExists() {
        when(repository.findByNameIgnoreCase("elektronik")).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service.createCategory(request("elektronik")))
                .isInstanceOf(CategoryAlreadyExistsException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Guncelleme: mevcut kategorinin alanlari degisir")
    void updateCategory_updatesFields() {
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.findByNameIgnoreCase("Tuketici Elektronigi")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = service.updateCategory(1L, request("Tuketici Elektronigi"));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Tuketici Elektronigi");
        assertThat(response.getDescription()).isEqualTo("Aciklama");
    }

    @Test
    @DisplayName("Guncelleme: isim baska bir kategoride varsa CategoryAlreadyExistsException firlatilir")
    void updateCategory_throws_whenNameBelongsToAnotherCategory() {
        Category other = Category.builder().id(2L).name("Mobilya").build();
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.findByNameIgnoreCase("Mobilya")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.updateCategory(1L, request("Mobilya")))
                .isInstanceOf(CategoryAlreadyExistsException.class);

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Guncelleme: kategori yoksa CategoryNotFoundException firlatilir")
    void updateCategory_throws_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCategory(99L, request("Mobilya")))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    @DisplayName("Silme: kategori varsa silinir")
    void deleteCategory_deletes_whenFound() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteCategory(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Silme: kategori yoksa CategoryNotFoundException firlatilir, silme yapilmaz")
    void deleteCategory_throws_whenNotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteCategory(99L))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
