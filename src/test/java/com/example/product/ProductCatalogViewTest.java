package com.example.product;

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.BrowserlessUIContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ProductCatalogViewTest {

    @Autowired
    private BrowserlessApplicationContext app;

    private BrowserlessUIContext window;

    @BeforeEach
    void openWindow() {
        window = app.newUser().newWindow();
    }

    @AfterEach
    void closeUser() {
        window.getUser().close();
    }

    @Test
    void selectRow_showsProductInDrawer() {
        window.navigate(ProductCatalogView.class);
        var grid = window.findGrid(ProductCatalogItem.class);

        grid.select(0);

        var nameField = window.findTextField().withLabel("Name");
        assertEquals(grid.getRow(0).name(), nameField.component().getValue());
    }

    @Test
    void saveNegativePrice_showsErrorMessage() {
        window.navigate(ProductCatalogView.class, 1L);
        var priceField = window.findBigDecimalField().withLabel("Price");

        priceField.setValue(new BigDecimal("-1.00"));
        window.findButton().withText("Save").click();

        assertTrue(priceField.component().isInvalid());
        assertEquals("Enter a price of 0 or more",
                priceField.component().getErrorMessage());
    }

    @Test
    void saveDuplicateSku_showsNotification() {
        window.navigate(ProductCatalogView.class, 1L);

        window.findTextField().withLabel("SKU").setValue("NBK-A5-DOT");
        window.findButton().withText("Save").click();

        assertEquals("The SKU is already in use. Please enter another one.",
                window.findNotification().getText());
    }
}
