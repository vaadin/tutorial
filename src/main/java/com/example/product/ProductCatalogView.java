package com.example.product;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Product Catalog")
class ProductCatalogView extends VerticalLayout {

    ProductCatalogView(ProductCatalogItemRepository repository) {
        // Create components
        var grid = new ProductCatalogGrid();
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
