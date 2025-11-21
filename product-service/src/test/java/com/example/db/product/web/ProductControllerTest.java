package com.example.db.product.web;

import com.example.db.product.application.ProductService;
import com.example.db.product.domain.Product;
import com.example.db.product.dto.ProductCreateRequest;
import com.example.db.product.dto.ProductUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    private MockMvc mockMvc;
    private ProductService productService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        productService = Mockito.mock(ProductService.class);
        objectMapper = new ObjectMapper();
        ProductController controller = new ProductController(productService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Product sample() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Sample");
        p.setCategory("Cat");
        p.setPrice(new BigDecimal("10.00"));
        p.setDescription("Desc");
        return p;
    }

    @Test
    void shouldReturnPagedProducts() throws Exception {
        Product p = sample();
        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

        when(productService.getProducts(eq(0), eq(10), anyList(), isNull(), isNull())).thenReturn(page);

        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(productService).getProducts(eq(0), eq(10), anyList(), isNull(), isNull());
    }

    @Test
    void shouldReturnPagedProductsWithFilters() throws Exception {
        Product p = sample();
        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

        when(productService.getProducts(eq(0), eq(10), anyList(), eq("chair"), eq("Electronics")))
                .thenReturn(page);

        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,desc")
                        .param("name", "chair")
                        .param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(productService).getProducts(eq(0), eq(10), anyList(), eq("chair"), eq("Electronics"));
    }

    @Test
    void shouldReturnSingleProduct() throws Exception {
        Product p = sample();
        when(productService.getProduct(1L)).thenReturn(p);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(productService).getProduct(1L);
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(
                "New", "Cat", new BigDecimal("20"), "Desc");

        Product returned = sample();
        returned.setName("New");
        returned.setPrice(new BigDecimal("20"));

        when(productService.createProduct(any(Product.class))).thenReturn(returned);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Updated", "Cat", new BigDecimal("30"), "Desc");

        Product existing = sample();
        Product updated = sample();
        updated.setName("Updated");
        updated.setPrice(new BigDecimal("30"));

        when(productService.getProduct(1L)).thenReturn(existing);
        when(productService.updateProduct(existing)).thenReturn(updated);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(productService).updateProduct(existing);
    }
}
