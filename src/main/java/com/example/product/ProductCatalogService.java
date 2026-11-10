package com.example.product;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional(readOnly = true)
class ProductCatalogService {

    private final ProductCatalogItemRepository productCatalogItemRepository;
    private final ProductDetailsRepository productDetailsRepository;

    public ProductCatalogService(
            ProductCatalogItemRepository productCatalogItemRepository,
            ProductDetailsRepository productDetailsRepository) {
        this.productCatalogItemRepository = productCatalogItemRepository;
        this.productDetailsRepository = productDetailsRepository;
    }

    public List<ProductCatalogItem> findItems(String searchTerm,
            Pageable pageable) {
        return productCatalogItemRepository
            .findByNameContainingIgnoreCase(searchTerm, pageable).getContent();
    }

    public Optional<ProductDetails> findDetailsById(Long id) {
        return productDetailsRepository.findById(id);
    }

    public Optional<ProductCatalogItem> findItemById(Long id) {
        return productCatalogItemRepository.findById(id);
    }

    @Transactional
    public ProductDetails save(@Valid ProductDetails productDetails) {
        return productDetailsRepository.save(new ProductDetails(productDetails));
    }

    @Transactional
    public void delete(ProductDetails productDetails) {
        productDetailsRepository.delete(productDetails);
    }
}
