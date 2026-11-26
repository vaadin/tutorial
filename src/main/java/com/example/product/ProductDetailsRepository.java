package com.example.product;

import org.springframework.data.repository.CrudRepository;

interface ProductDetailsRepository
    extends CrudRepository<ProductDetails, Long> {
}
