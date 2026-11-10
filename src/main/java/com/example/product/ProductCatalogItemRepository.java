package com.example.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

interface ProductCatalogItemRepository
    extends PagingAndSortingRepository<ProductCatalogItem, Long> {

    Optional<ProductCatalogItem> findById(Long id);
    Slice<ProductCatalogItem> findByNameContainingIgnoreCase(String name,
        Pageable pageable);
}
