package com.example.product;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

class ProductFormDrawer extends VerticalLayout {

    private final ProductForm form;

    ProductFormDrawer() {
        var header = new H2("Product Details");
        form = new ProductForm();

        add(header);
        add(new Scroller(form));
        setHeightFull();
    }

    public void setProductDetails(ProductDetails productDetails) {
        form.setFormDataObject(productDetails);
    }
}
