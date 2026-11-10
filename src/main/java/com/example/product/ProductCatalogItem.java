package com.example.product;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("PRODUCTS")
record ProductCatalogItem(
    @Id long productId,
    String name,
    String description,
    String category,
    String brand,
    BigDecimal price
) {
}
