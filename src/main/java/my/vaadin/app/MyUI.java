package my.vaadin.app;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;

import java.util.List;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

    private CustomerService service = CustomerService.getInstance();
    private Grid grid = new Grid();

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();

        grid.setColumns("firstName", "lastName", "email");
        // add Grid to the layout
        layout.addComponents(grid);

        updateList();

        layout.setMargin(true);
        setContent(layout);
    }

    public void updateList() {
        // fetch list of Customers from service and assign it to Grid
        List<Customer> customers = service.findAll();
        grid.setContainerDataSource(new BeanItemContainer<>(Customer.class, customers));
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
