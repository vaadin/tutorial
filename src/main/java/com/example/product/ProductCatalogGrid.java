package com.example.product;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;

class ProductCatalogGrid extends Grid<ProductCatalogItem> {

    ProductCatalogGrid() {
        addColumn(ProductCatalogItem::name)
            .setHeader("Name");
        addColumn(ProductCatalogItem::price)
            .setHeader("Price")
            .setTextAlign(ColumnTextAlign.END);
        addColumn(ProductCatalogItem::description)
            .setHeader("Description");
        addColumn(ProductCatalogItem::category)
            .setHeader("Category");
        addColumn(ProductCatalogItem::brand)
            .setHeader("Brand");
    }
}
