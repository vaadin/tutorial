package com.example.product;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Product Catalog")
class ProductCatalogView extends VerticalLayout {

    ProductCatalogView(ProductCatalogItemRepository repository) {
        // Create components
        var searchField = new SearchField();

        var grid = new ProductCatalogGrid();
        grid.setItemsPageable(pageable -> repository
            .findByNameContainingIgnoreCase(searchField.getValue(),
                pageable)
            .getContent()
        );

        searchField.addValueChangeListener(e ->
                grid.getDataProvider().refreshAll());

        // Layout view
        setSizeFull();
        grid.setSizeFull();
        add(searchField);
        add(grid);
    }

    static class SearchField extends TextField {

        SearchField() {
            setPlaceholder("Search");
            setPrefixComponent(VaadinIcon.SEARCH.create());
            setValueChangeMode(ValueChangeMode.LAZY);
        }
    }
}
