package my.vaaadin.app;

import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 *
 */
@Theme("mytheme")
@Widgetset("my.vaaadin.app.MyAppWidgetset")
public class MyUI extends UI {

	private CustomerService service = CustomerService.getInstance();
	private Grid grid = new Grid();

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		final VerticalLayout layout = new VerticalLayout();

		grid.setColumns("firstName", "lastName", "email");
		// add Grid to the layout
		layout.addComponent(grid);

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
