package com.example.api.products.application;

import com.example.api.products.client.ProductDbClient;
import com.example.api.products.dto.ProductDto;
import com.example.api.products.dto.ProductSearchCriteria;
import com.example.common.product.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductDbClient productDbClient;

    public Mono<PageResponse<ProductDto>> getProducts(ProductSearchCriteria criteria) {
        return productDbClient.getProducts(
                criteria.page(),
                criteria.size(),
                criteria.sort(),
                criteria.name(),
                criteria.category()
        );
    }

    public Mono<ProductDto> getProduct(Long id) {
        return productDbClient.getProductById(id);
    }

    public Mono<ProductDto> create(ProductDto dto) {
        return productDbClient.createProduct(dto);
    }

    public Mono<ProductDto> update(Long id, ProductDto dto) {
        return productDbClient.updateProduct(id, dto);
    }

    public Mono<Void> delete(Long id) {
        return productDbClient.deleteProduct(id);
    }
}