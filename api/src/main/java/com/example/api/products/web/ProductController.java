package com.example.api.products.web;

import com.example.api.products.application.ProductService;
import com.example.api.products.dto.PageResponse;
import com.example.api.products.dto.ProductDto;
import com.example.api.products.dto.ProductSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get products", security = @SecurityRequirement(name = "bearerAuth"))
    public Mono<ResponseEntity<PageResponse<ProductDto>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, name = "sort", defaultValue = "name") List<String> sortParams,
            @RequestParam(required = false, name = "name") String name,
            @RequestParam(required = false, name = "category") String category
    ) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(page, size, sortParams, name, category);
        return productService.getProducts(criteria)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id", security = @SecurityRequirement(name = "bearerAuth"))
    public Mono<ResponseEntity<ProductDto>> getById(@PathVariable Long id) {
        return productService.getProduct(id)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    @Operation(summary = "Create product", security = @SecurityRequirement(name = "bearerAuth"))
    public Mono<ResponseEntity<ProductDto>> create(@Valid @RequestBody ProductDto dto) {
        return productService.create(dto)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", security = @SecurityRequirement(name = "bearerAuth"))
    public Mono<ResponseEntity<ProductDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto dto
    ) {
        return productService.update(id, dto)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product", security = @SecurityRequirement(name = "bearerAuth"))
    public Mono<Void> delete(@PathVariable Long id) {
        return productService.delete(id);
    }
}
