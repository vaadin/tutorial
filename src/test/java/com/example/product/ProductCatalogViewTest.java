package com.example.product;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ProductCatalogViewTest extends SpringBrowserlessTest {

    @Test
    void selectRow_showsProductInDrawer() {
        navigate(ProductCatalogView.class);
        var grid = test(find(Grid.class).single(),
                ProductCatalogItem.class);

        grid.select(0);

        var nameField = find(TextField.class).withLabel("Name").single();
        assertEquals(grid.getRow(0).name(), nameField.getValue());
    }

    @Test
    void saveNegativePrice_showsErrorMessage() {
        navigate(ProductCatalogView.class, 1L);
        var priceField = find(BigDecimalField.class).withLabel("Price").single();

        test(priceField).setValue(new BigDecimal("-1.00"));
        test(find(Button.class).withText("Save").single()).click();

        assertTrue(priceField.isInvalid());
        assertEquals("Enter a price of 0 or more", priceField.getErrorMessage());
    }

    @Test
    void saveDuplicateSku_showsNotification() {
        navigate(ProductCatalogView.class, 1L);
        var skuField = find(TextField.class).withLabel("SKU").single();

        test(skuField).setValue("NBK-A5-DOT");
        test(find(Button.class).withText("Save").single()).click();

        var notification = find(Notification.class).single();
        assertEquals("The SKU is already in use. Please enter another one.",
                test(notification).getText());
    }
}
