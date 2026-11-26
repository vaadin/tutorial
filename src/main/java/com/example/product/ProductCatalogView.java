package com.example.product;

import com.example.base.ui.ViewTitle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Objects;
import java.util.Optional;

@Route("products")
@RouteAlias("")
@PageTitle("Product Catalog")
@Menu(order = 1, icon = "vaadin:package")
class ProductCatalogView extends MasterDetailLayout implements HasUrlParameter<Long> {

    private final ProductCatalogService service;
    private final ProductCatalogGrid grid;

    ProductCatalogView(ProductCatalogService service) {
        this.service = service;

        // Create components
        var title = new ViewTitle("Product Catalog");

        var searchField = new SearchField();

        grid = new ProductCatalogGrid();
        grid.setItemsPageable(pageable -> service
            .findItems(searchField.getValue(), pageable)
        ).setIdentifierProvider(ProductCatalogItem::productId);

        var drawer = new ProductFormDrawer(productDetails -> {
            var saved = service.save(productDetails);
            grid.getDataProvider().refreshAll();
            return saved;
        }, productDetails -> {
            service.delete(productDetails);
            grid.deselectAll();
            grid.getDataProvider().refreshAll();
        }, this::handleException);

        searchField.addValueChangeListener(e ->
                grid.getDataProvider().refreshAll());

        grid.addSelectionListener(e -> {
            var productDetails = e.getFirstSelectedItem()
                .flatMap(item -> service.findDetailsById(item.productId()));
            productDetails.ifPresent(drawer::setProductDetails);
            setDetail(productDetails.isPresent() ? drawer : null);
            e.getFirstSelectedItem()
                .map(ProductCatalogItem::productId)
                .ifPresentOrElse(
                    ProductCatalogView::showProductDetails,
                    ProductCatalogView::showProductCatalog
                );
        });
        addBackdropClickListener(e -> grid.deselectAll());
        addDetailEscapePressListener(e -> grid.deselectAll());

        var addButton = new Button("Add Product", e ->
            new AddProductDialog(
                productDetails -> {
                    var saved = service.save(productDetails);
                    grid.getDataProvider().refreshAll();
                    service.findItemById(saved.getProductId())
                           .ifPresent(grid::select);
                },
                this::handleException
            ).open()
        );

        // Layout view
        setSizeFull();
        setDetailSize("300px");
        setExpandMaster(true);

        var toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setWrap(true);
        toolbar.addToStart(title, searchField);
        toolbar.addToEnd(addButton);

        var listLayout = new VerticalLayout(toolbar, grid);
        listLayout.setSizeFull();
        grid.setSizeFull();

        setMaster(listLayout);
    }

    private void handleException(RuntimeException exception) {
        if (exception instanceof OptimisticLockingFailureException) {
            new WarningNotification("Another user has edited the same product. "
                + "Please refresh and try again.").open();
        } else if (exception instanceof DuplicateKeyException) {
            new WarningNotification(
                "The SKU is already in use. Please enter another one.").open();
        } else {
            // Delegate to Vaadin's default error handler
            throw exception;
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long productId) {
        var selectedId = grid.asSingleSelect().getOptionalValue()
            .map(ProductCatalogItem::productId)
            .orElse(null);
        if (Objects.equals(productId, selectedId)) {
            return;
        }
        Optional.ofNullable(productId)
            .flatMap(service::findItemById)
            .ifPresentOrElse(grid::select, grid::deselectAll);
    }

    public static void showProductDetails(long productId) {
        UI.getCurrentOrThrow().navigate(ProductCatalogView.class, productId);
    }

    public static void showProductCatalog() {
        UI.getCurrentOrThrow().navigate(ProductCatalogView.class);
    }

    static class SearchField extends TextField {

        SearchField() {
            setPlaceholder("Search");
            setPrefixComponent(VaadinIcon.SEARCH.create());
            setValueChangeMode(ValueChangeMode.LAZY);
        }
    }

    static class WarningNotification extends Notification {

        WarningNotification(String text) {
            super(text, 3000, Position.MIDDLE);
            addThemeVariants(NotificationVariant.WARNING);
        }
    }
}
