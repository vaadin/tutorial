package com.example.product;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

class ProductFormDrawer extends Composite<VerticalLayout> {

    @FunctionalInterface
    interface SaveCallback {
        ProductDetails save(ProductDetails productDetails);
    }

    @FunctionalInterface
    interface DeleteCallback {
        void delete(ProductDetails productDetails);
    }

    @FunctionalInterface
    interface ErrorCallback {
        void handleException(RuntimeException e);
    }

    private final SaveCallback saveCallback;
    private final DeleteCallback deleteCallback;
    private final ErrorCallback errorCallback;
    private final ProductForm form;
    private ProductDetails productDetails;

    ProductFormDrawer(SaveCallback saveCallback, DeleteCallback deleteCallback,
                      ErrorCallback errorCallback) {
        this.saveCallback = saveCallback;
        this.deleteCallback = deleteCallback;
        this.errorCallback = errorCallback;

        var header = new H2("Product Details");
        form = new ProductForm();

        var saveButton = new Button("Save", e -> save());
        saveButton.addThemeVariants(ButtonVariant.PRIMARY);
        var deleteButton = new Button("Delete", e -> delete());
        deleteButton.addThemeVariants(ButtonVariant.ERROR);

        var layout = getContent();
        layout.add(header);
        layout.add(new Scroller(form));
        layout.add(new HorizontalLayout(saveButton, deleteButton));
        layout.setHeightFull();
    }

    public void setProductDetails(ProductDetails productDetails) {
        this.productDetails = productDetails;
        form.setFormDataObject(productDetails);
    }

    private void save() {
        form.getFormDataObject().ifPresent(productDetails -> {
            try {
                var saved = saveCallback.save(productDetails);
                setProductDetails(saved);
            } catch (RuntimeException e) {
                errorCallback.handleException(e);
            }
        });
    }

    private void delete() {
        var dialog = new ConfirmDialog();
        dialog.setHeader("Delete Product");
        dialog.setText("Are you sure you want to permanently delete this product?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            try {
                deleteCallback.delete(productDetails);
            } catch (RuntimeException e) {
                errorCallback.handleException(e);
            }
        });
        dialog.open();
    }
}
