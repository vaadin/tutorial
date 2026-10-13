package com.example.product;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ProductCatalogServiceTest {

    @Autowired
    private ProductCatalogService service;

    @Test
    void save_validProduct_assignsId() {
        var saved = service.save(newProduct());
        assertNotNull(saved.getProductId());
    }

    @Test
    void save_negativePrice_throwsConstraintViolation() {
        var product = newProduct();
        product.setPrice(new BigDecimal("-1.00"));
        assertThrows(ConstraintViolationException.class,
                () -> service.save(product));
    }

    @Test
    void save_duplicateSku_throwsDuplicateKey() {
        var product = newProduct();
        product.setSku("NBK-A5-DOT");
        assertThrows(DuplicateKeyException.class,
                () -> service.save(product));
    }

    private static ProductDetails newProduct() {
        var product = new ProductDetails();
        product.setName("Travel Mug");
        product.setDescription("Insulated travel mug, 400 ml");
        product.setCategory("Home & Kitchen");
        product.setSku("MUG-400-TRV");
        product.setPrice(new BigDecimal("14.90"));
        return product;
    }
}
