package com.example.product;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.jspecify.annotations.Nullable;

class ProductForm extends Composite<FormLayout>  {

    private final Binder<ProductDetails> binder;

    ProductForm() {
        // Create components
        var nameField = new TextField("Name");
        var descriptionField = new TextArea("Description");
        var categoryField = new TextField("Category");
        var brandField = new TextField("Brand");
        var skuField = new TextField("SKU");
        var releaseDateField = new DatePicker("Release Date");
        var priceField = new BigDecimalField("Price");
        var discountField = new BigDecimalField("Discount");

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
              .bind(ProductDetails::getName,
                    ProductDetails::setName);
        binder.forField(descriptionField)
              .bind(ProductDetails::getDescription,
                    ProductDetails::setDescription);
        binder.forField(categoryField)
              .bind(ProductDetails::getCategory,
                    ProductDetails::setCategory);
        binder.forField(brandField)
              .bind(ProductDetails::getBrand,
                    ProductDetails::setBrand);
        binder.forField(skuField)
              .bind(ProductDetails::getSku,
                    ProductDetails::setSku);
        binder.forField(releaseDateField)
              .bind(ProductDetails::getReleaseDate,
                    ProductDetails::setReleaseDate);
        binder.forField(priceField)
              .bind(ProductDetails::getPrice,
                    ProductDetails::setPrice);
        binder.forField(discountField)
              .bind(ProductDetails::getDiscount,
                    ProductDetails::setDiscount);
        binder.setReadOnly(true);
    }

    public void setFormDataObject(@Nullable ProductDetails productDetails) {
        binder.setBean(productDetails);
    }
}
