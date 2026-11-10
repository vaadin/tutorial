package com.example.base.ui;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.ScrollerVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;

@Layout
class MainLayout extends AppLayout {

    MainLayout() {
        // Create components
        var appLogo = new Avatar("Click & Collect");
        appLogo.addClassName("app-logo");
        appLogo.addThemeVariants(AvatarVariant.AURA_FILLED, AvatarVariant.XSMALL);

        var appName = new Span("Click & Collect");
        appName.addClassName("app-name");

        var menu = new SideNav();
        menu.setMinWidth(200, Unit.PIXELS);
        MenuConfiguration.getMenuEntries().forEach(entry -> {
            var item = new SideNavItem(entry.title(), entry.menuClass(),
                new Icon(entry.icon()));
            item.setMatchNested(true);
            menu.addItem(item);
        });

        // Layout drawer
        var header = new HorizontalLayout(appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);

        var scroller = new Scroller(menu);
        scroller.addThemeVariants(ScrollerVariant.OVERFLOW_INDICATORS);

        setPrimarySection(Section.DRAWER);
        addToDrawer(header, scroller);
    }
}
