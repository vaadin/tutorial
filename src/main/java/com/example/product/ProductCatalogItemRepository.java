package com.example.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import org.springframework.data.repository.PagingAndSortingRepository;

interface ProductCatalogItemRepository
    extends PagingAndSortingRepository<ProductCatalogItem, Long> {

    Slice<ProductCatalogItem> findByNameContainingIgnoreCase(String name,
        Pageable pageable);
}
