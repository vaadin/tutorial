package com.example.product;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table("PRODUCTS")
class ProductDetails {

    @Id
    private Long productId;
    @Version
    private Long version;
    @NotBlank @Size(max = 100)
    private String name;
    @NotBlank @Size(max = 500)
    private String description;
    @NotBlank @Size(max = 100)
    private String category;
    @Size(max = 100)
    private String brand;
    @NotBlank @Size(max = 50)
    private String sku;
    private LocalDate releaseDate;
    @NotNull @PositiveOrZero
    private BigDecimal price;
    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal discount;

    public ProductDetails() {
    }

    public ProductDetails(ProductDetails original) {
        this.productId = original.productId;
        this.version = original.version;
        this.name = original.name;
        this.description = original.description;
        this.category = original.category;
        this.brand = original.brand;
        this.sku = original.sku;
        this.releaseDate = original.releaseDate;
        this.price = original.price;
        this.discount = original.discount;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
}
