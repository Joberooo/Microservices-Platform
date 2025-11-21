package com.example.db.product.web;

import com.example.common.product.dto.PageResponse;
import com.example.db.product.application.ProductService;
import com.example.db.product.dto.ProductCreateRequest;
import com.example.db.product.dto.ProductResponseDto;
import com.example.db.product.dto.ProductUpdateRequest;
import com.example.db.product.mapper.ProductMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        return toDtoPage(
                productService.getProducts(page, size, sortParams, name, category)
        );
    }

    @GetMapping("/{id}")
    public ProductResponseDto getProduct(@PathVariable Long id) {
        return ProductMapper.toDto(
                productService.getProduct(id)
        );
    }

    @PostMapping
    public ProductResponseDto createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return ProductMapper.toDto(
                productService.createProduct(request)
        );
    }

    @PutMapping("/{id}")
    public ProductResponseDto updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ProductMapper.toDto(
                productService.updateProduct(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
