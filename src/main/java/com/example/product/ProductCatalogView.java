package com.example.product;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Product Catalog")
class ProductCatalogView extends VerticalLayout {

    ProductCatalogView(ProductCatalogItemRepository repository) {
        // Create components
        var grid = new Grid<ProductCatalogItem>();
        grid.addColumn(ProductCatalogItem::name)
            .setHeader("Name");
        grid.addColumn(ProductCatalogItem::price)
            .setHeader("Price")
            .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(ProductCatalogItem::description)
            .setHeader("Description");
        grid.addColumn(ProductCatalogItem::category)
            .setHeader("Category");
        grid.addColumn(ProductCatalogItem::brand)
            .setHeader("Brand");
        grid.setItemsPageable(pageable -> repository
            .findAll(pageable)
            .getContent()
        );

        // Layout view
        setSizeFull();
        grid.setSizeFull();
        add(grid);
    }
}
