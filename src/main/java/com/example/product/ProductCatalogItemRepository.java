package com.example.product;

import org.springframework.data.repository.PagingAndSortingRepository;

interface ProductCatalogItemRepository
    extends PagingAndSortingRepository<ProductCatalogItem, Long> {
}
