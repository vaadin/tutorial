package com.vaadin.starter.skeleton;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.starter.skeleton.backend.Customer;
import com.vaadin.starter.skeleton.backend.CustomerService;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {
	private CustomerService service = CustomerService.getInstance();
    private Grid<Customer> grid = new Grid<>();
    private TextField filterText = new TextField();
    private CustomerForm form = new CustomerForm(this);

    public MainView() {
		filterText.setPlaceholder("Filter by name...");
        filterText.setValueChangeMode(ValueChangeMode.EAGER);
        filterText.addValueChangeListener(e -> updateList());
        Button clearFilterTextBtn = new Button(
                new Icon(VaadinIcon.CLOSE_CIRCLE));

        clearFilterTextBtn.addClickListener(e -> filterText.clear());

        HorizontalLayout filtering = new HorizontalLayout(filterText,
                clearFilterTextBtn);
        Button addCustomerBtn = new Button("Add new customer");
        addCustomerBtn.addClickListener(e -> {
            grid.asSingleSelect().clear();
            form.setCustomer(new Customer());
        });
        add(filtering, grid);

        HorizontalLayout toolbar = new HorizontalLayout(filtering,
                addCustomerBtn);


        grid.setSizeFull();

        grid.addColumn(Customer::getFirstName).setHeader("First name");
        grid.addColumn(Customer::getLastName).setHeader("Last name");
        grid.addColumn(Customer::getStatus).setHeader("Status");

        HorizontalLayout main = new HorizontalLayout(grid, form);
        main.setAlignItems(Alignment.START);
        main.setSizeFull();

        add(toolbar, main);
        add(filtering, main);
        setHeight("100vh");
        updateList();

        grid.asSingleSelect().addValueChangeListener(event -> {
            form.setCustomer(event.getValue());
        });

    }

    public void updateList() {
        /**
         * Note that filterText.getValue() might return null; in this case, the backend
         * takes care of it for us
         */
        grid.setItems(service.findAll(filterText.getValue()));
    }

}