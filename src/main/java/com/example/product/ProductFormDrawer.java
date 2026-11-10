package com.example.product;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

class ProductFormDrawer extends Composite<VerticalLayout> {

    private final ProductForm form;

    ProductFormDrawer() {
        var header = new H2("Product Details");
        form = new ProductForm();

        var layout = getContent();
        layout.add(header);
        layout.add(new Scroller(form));
        layout.setHeightFull();
    }

    public void setProductDetails(ProductDetails productDetails) {
        form.setFormDataObject(productDetails);
    }
}
