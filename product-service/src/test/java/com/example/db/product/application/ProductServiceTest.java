package com.example.db.product.application;

import com.example.db.exceptions.ProductNotFoundException;
import com.example.db.product.domain.Product;
import com.example.db.product.domain.ProductRepository;
import com.example.db.product.dto.ProductCreateRequest;
import com.example.db.product.dto.ProductUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    void getProductsShouldUsePageableWithParsedSortParams_multiFieldSyntax() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        productService.getProducts(0, 10, List.of("name,desc", "price,asc"), null, null);

        verify(productRepository).findAll(captor.capture());
        verifyNoMoreInteractions(productRepository);

        Pageable pageable = captor.getValue();
        List<Sort.Order> orders = pageable.getSort().toList();

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getProperty()).isEqualTo("name");
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(orders.get(1).getProperty()).isEqualTo("price");
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getProductsShouldHandleTwoParamSortSyntax_fieldThenDirection() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        productService.getProducts(0, 10, List.of("name", "desc"), null, null);

        verify(productRepository).findAll(captor.capture());
        verifyNoMoreInteractions(productRepository);

        Pageable pageable = captor.getValue();
        List<Sort.Order> orders = pageable.getSort().toList();

        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getProperty()).isEqualTo("name");
        assertThat(orders.getFirst().getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getProductsShouldReturnUnsortedWhenSortNull() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        productService.getProducts(0, 10, null, null, null);

        verify(productRepository).findAll(captor.capture());
        verifyNoMoreInteractions(productRepository);

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
        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void getProductShouldThrowWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(99L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void createProductShouldMapAndSave() {
        ProductCreateRequest request =
                new ProductCreateRequest("New", "Cat", new BigDecimal("20"), "Desc");

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product created = productService.createProduct(request);

        assertThat(created.getName()).isEqualTo("New");
        assertThat(created.getCategory()).isEqualTo("Cat");
        assertThat(created.getPrice()).isEqualByComparingTo("20");
        assertThat(created.getDescription()).isEqualTo("Desc");

        verify(productRepository).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void updateProductShouldUpdateExistingAndSave() {
        ProductUpdateRequest request =
                new ProductUpdateRequest("Updated", "NewCat", new BigDecimal("30"), "NewDesc");

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product updated = productService.updateProduct(1L, request);

        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getCategory()).isEqualTo("NewCat");
        assertThat(updated.getPrice()).isEqualByComparingTo("30");
        assertThat(updated.getDescription()).isEqualTo("NewDesc");

        verify(productRepository).findById(1L);
        verify(productRepository).save(sampleProduct);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void updateProductShouldThrowWhenMissing() {
        ProductUpdateRequest request =
                new ProductUpdateRequest("Updated", "Cat", new BigDecimal("30"), "Desc");

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(1L, request))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void deleteShouldAlwaysDelegateToRepository() {
        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
        verifyNoMoreInteractions(productRepository);
    }
}
