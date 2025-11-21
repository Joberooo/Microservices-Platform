package com.example.db.product.application;

import com.example.db.exceptions.ProductNotFoundException;
import com.example.db.product.domain.Product;
import com.example.db.product.domain.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        productService = new ProductService(productRepository);

        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Sample");
        sampleProduct.setCategory("Category");
        sampleProduct.setPrice(new BigDecimal("10.00"));
        sampleProduct.setDescription("Description");
    }

    @Test
    void getProductsShouldUsePageableWithParsedSortParams() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        productService.getProducts(0, 10, List.of("name,desc", "price,asc"), null, null);

        verify(productRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();
        List<Sort.Order> orders = pageable.getSort().toList();

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getProperty()).isEqualTo("name");
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);

        assertThat(orders.get(1).getProperty()).isEqualTo("price");
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getProductsShouldReturnUnsortedWhenSortNull() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        productService.getProducts(0, 10, null, null, null);

        verify(productRepository).findAll(captor.capture());
        Pageable pageable = captor.getValue();

        assertThat(pageable.getSort().isUnsorted()).isTrue();
    }

    @Test
    void getProductsShouldUseNameFilterWhenOnlyNameProvided() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.findByNameContainingIgnoreCase(eq("Sample"), any(Pageable.class)))
                .thenReturn(page);

        productService.getProducts(0, 10, List.of("name,asc"), "Sample", null);

        verify(productRepository).findByNameContainingIgnoreCase(eq("Sample"), any(Pageable.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void getProductsShouldUseCategoryFilterWhenOnlyCategoryProvided() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.findByCategoryContainingIgnoreCase(eq("Category"), any(Pageable.class)))
                .thenReturn(page);

        productService.getProducts(0, 10, List.of("name,asc"), null, "Category");

        verify(productRepository).findByCategoryContainingIgnoreCase(eq("Category"), any(Pageable.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void getProductsShouldUseNameAndCategoryFiltersWhenBothProvided() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(
                eq("Sample"), eq("Category"), any(Pageable.class)))
                .thenReturn(page);

        productService.getProducts(0, 10, List.of("name,asc"), "Sample", "Category");

        verify(productRepository).findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(
                eq("Sample"), eq("Category"), any(Pageable.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void getProductShouldReturnExisting() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        Product found = productService.getProduct(1L);
        assertThat(found).isSameAs(sampleProduct);
    }

    @Test
    void getProductShouldThrowWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.getProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void updateProductShouldThrowWhenNotExists() {
        when(productRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> productService.updateProduct(sampleProduct))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void deleteShouldDelegateWhenExists() {
        when(productRepository.existsById(1L)).thenReturn(true);
        productService.deleteProduct(1L);
        verify(productRepository).deleteById(1L);
    }
}
