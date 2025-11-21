package com.example.db.product.web;

import com.example.db.product.application.ProductService;
import com.example.db.product.domain.Product;
import com.example.db.product.dto.*;
import com.example.db.product.mapper.ProductMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.db.product.mapper.ProductMapper.toDtoPage;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public PageResponse<ProductResponseDto> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, name = "sort") List<String> sortParams,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category
    ) {
        Page<Product> products = productService.getProducts(page, size, sortParams, name, category);
        return toDtoPage(products);
    }

    @GetMapping("/{id}")
    public ProductResponseDto getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        return ProductMapper.toDto(product);
    }

    @PostMapping
    public ProductResponseDto createProduct(@Valid @RequestBody ProductCreateRequest request) {
        Product toSave = ProductMapper.toEntity(request);
        Product saved = productService.createProduct(toSave);
        return ProductMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public ProductResponseDto updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        Product existing = productService.getProduct(id);
        ProductMapper.updateEntity(existing, request);
        Product updated = productService.updateProduct(existing);
        return ProductMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
