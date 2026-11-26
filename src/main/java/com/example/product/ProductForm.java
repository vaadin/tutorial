package com.example.product;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.BigDecimalRangeValidator;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Optional;

class ProductForm extends Composite<FormLayout>  {

    private final Binder<ProductDetails> binder;

    ProductForm() {
        // Create components
        var nameField = new TextField("Name");
        nameField.setMaxLength(100);
        var descriptionField = new TextArea("Description");
        descriptionField.setMaxLength(500);
        var categoryField = new TextField("Category");
        categoryField.setMaxLength(100);
        var brandField = new TextField("Brand");
        brandField.setMaxLength(100);
        var skuField = new TextField("SKU");
        skuField.setMaxLength(50);
        var releaseDateField = new DatePicker("Release Date");
        var priceField = new BigDecimalField("Price");
        var discountField = new BigDecimalField("Discount");
        discountField.setSuffixComponent(new Span("%"));

        // Layout form
        var layout = getContent();
        layout.add(nameField);
        layout.add(descriptionField);
        layout.add(categoryField);
        layout.add(brandField);
        layout.add(skuField);
        layout.add(releaseDateField);
        layout.add(priceField);
        layout.add(discountField);

        // Bind fields
        binder = new Binder<>();
        binder.forField(nameField)
              .asRequired("Enter name")
              .bind(ProductDetails::getName,
                    ProductDetails::setName);
        binder.forField(descriptionField)
              .asRequired("Enter description")
              .bind(ProductDetails::getDescription,
                    ProductDetails::setDescription);
        binder.forField(categoryField)
              .asRequired("Enter category")
              .bind(ProductDetails::getCategory,
                    ProductDetails::setCategory);
        binder.forField(brandField)
              .bind(ProductDetails::getBrand,
                    ProductDetails::setBrand);
        binder.forField(skuField)
              .asRequired("Enter SKU")
              .bind(ProductDetails::getSku,
                    ProductDetails::setSku);
        binder.forField(releaseDateField)
              .bind(ProductDetails::getReleaseDate,
                    ProductDetails::setReleaseDate);
        binder.forField(priceField)
              .asRequired("Enter price")
              .withValidator(new BigDecimalRangeValidator(
                  "Enter a price of 0 or more", BigDecimal.ZERO, null))
              .bind(ProductDetails::getPrice,
                    ProductDetails::setPrice);
        binder.forField(discountField)
              .withValidator(new BigDecimalRangeValidator(
                  "Enter a discount between 0 and 100",
                  BigDecimal.ZERO, BigDecimal.valueOf(100)))
              .bind(ProductDetails::getDiscount,
                    ProductDetails::setDiscount);
    }

    public void setFormDataObject(@Nullable ProductDetails productDetails) {
        binder.setBean(productDetails);
    }

    public Optional<ProductDetails> getFormDataObject() {
        if (binder.getBean() == null) {
            throw new IllegalStateException("No form data object");
        }
        if (binder.validate().isOk()) {
            return Optional.of(binder.getBean());
        } else {
            return Optional.empty();
        }
    }
}
