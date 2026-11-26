package com.example.base.ui;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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
        var scroller = new Scroller(new MainMenu());
        scroller.addThemeVariants(ScrollerVariant.OVERFLOW_INDICATORS);

        setPrimarySection(Section.DRAWER);
        addToDrawer(new DrawerHeader(), scroller);
    }

    static class DrawerHeader extends HorizontalLayout {

        DrawerHeader() {
            var appLogo = new Avatar("Click & Collect");
            appLogo.addClassName("app-logo");
            appLogo.addThemeVariants(AvatarVariant.AURA_FILLED, AvatarVariant.XSMALL);

            var appName = new Span("Click & Collect");
            appName.addClassName("app-name");

            add(appLogo, appName);
            setAlignItems(Alignment.CENTER);
            setPadding(true);
        }
    }

    static class MainMenu extends SideNav {

        MainMenu() {
            setMinWidth(200, Unit.PIXELS);
            MenuConfiguration.getMenuEntries().forEach(entry -> {
                var item = new SideNavItem(entry.title(), entry.menuClass(),
                    new Icon(entry.icon()));
                item.setMatchNested(true);
                addItem(item);
            });
        }
    }
}
