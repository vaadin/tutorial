package com.example.order;

import com.example.base.ui.ViewTitle;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("orders")
@PageTitle("Orders")
@Menu(order = 2, icon = "vaadin:cart")
class OrdersView extends VerticalLayout {

    OrdersView() {
        add(new ViewTitle("Orders"));
        add(new Paragraph("Customer orders will be listed here."));
    }
}
