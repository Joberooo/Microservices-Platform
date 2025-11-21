package com.example.db.product.application;

import com.example.db.exceptions.ProductNotFoundException;
import com.example.db.product.domain.Product;
import com.example.db.product.domain.ProductRepository;
import com.example.db.product.dto.ProductCreateRequest;
import com.example.db.product.dto.ProductUpdateRequest;
import com.example.db.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> getProducts(int page, int size, List<String> sortParams, String name, String category) {
        Sort sort = parseSort(sortParams);
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean hasName = name != null && !name.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (hasName && hasCategory) {
            return productRepository
                    .findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(name, category, pageable);
        } else if (hasName) {
            return productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (hasCategory) {
            return productRepository.findByCategoryContainingIgnoreCase(category, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product createProduct(ProductCreateRequest request) {
        return productRepository.save(ProductMapper.toEntity(request));
    }

    public Product updateProduct(Long id, ProductUpdateRequest request) {
        Product product = getProduct(id);
        ProductMapper.updateEntity(product, request);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private Sort parseSort(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        if (sortParams.size() == 2
                && !sortParams.get(0).contains(",")
                && ("asc".equalsIgnoreCase(sortParams.get(1).trim())
                || "desc".equalsIgnoreCase(sortParams.get(1).trim()))) {

            String field = sortParams.get(0).trim();
            String dirToken = sortParams.get(1).trim();
            Sort.Direction direction = "desc".equalsIgnoreCase(dirToken)
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            orders.add(new Sort.Order(direction, field));
            return Sort.by(orders);
        }
        for (String param : sortParams) {
            if (param == null || param.isBlank()) {
                continue;
            }
            String[] parts = param.split(",");
            if (parts.length == 1 && ("asc".equalsIgnoreCase(parts[0]) || "desc".equalsIgnoreCase(parts[0]))) {
                continue;
            }
            String field = parts[0].trim();
            Sort.Direction direction = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc"))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            orders.add(new Sort.Order(direction, field));
        }
        if (orders.isEmpty()) {
            return Sort.unsorted();
        }
        return Sort.by(orders);
    }
}
