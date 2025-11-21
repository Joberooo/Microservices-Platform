package com.example.api.products.dto;

import java.util.List;

public record ProductSearchCriteria(
        int page,
        int size,
        List<String> sort,
        String name,
        String category
) {}
