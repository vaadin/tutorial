package com.example.application.views;

import com.example.application.views.lobby.LobbyView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.spring.security.AuthenticationContext;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;
    private H2 viewTitle;

    public MainLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addNavbarContent();
    }

    private void addNavbarContent() {
        var toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        toggle.setTooltipText("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(FontSize.LARGE, Margin.NONE, Flex.GROW);

        var logout = new Button("Logout " + authenticationContext.getPrincipalName().orElse(""), event -> authenticationContext.logout());

        var header = new Header(toggle, viewTitle, logout);
        header.addClassNames(AlignItems.CENTER, Display.FLEX, Padding.End.MEDIUM, Width.FULL);

        addToNavbar(false, header);
    }

    private void addDrawerContent() {
        var appName = new Span("Vaadin Chat");
        appName.addClassNames(AlignItems.CENTER, Display.FLEX, FontSize.LARGE, FontWeight.SEMIBOLD, Height.XLARGE,
                Padding.Horizontal.MEDIUM);

        addToDrawer(appName, new Scroller(createSideNav()));
    }

    private SideNav createSideNav() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Lobby", LobbyView.class, VaadinIcon.BUILDING.create()));

        return nav;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        if (getContent() == null) {
            return "";
        } else if (getContent() instanceof HasDynamicTitle titleHolder) {
            return titleHolder.getPageTitle();
        } else {
            var title = getContent().getClass().getAnnotation(PageTitle.class);
            return title == null ? "" : title.value();
        }
    }
}
