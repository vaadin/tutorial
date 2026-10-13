package com.example.base.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class ViewTitle extends Composite<HorizontalLayout> {

    public ViewTitle(String title) {
        addClassName("view-title");

        var layout = getContent();
        layout.add(new DrawerToggle(), new H1(title));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
    }
}
