package net.link.safeonline.demo.cinema.webapp;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class Layout extends WebPage<Object> {

	private static final long serialVersionUID = 1L;

	public Layout() {
		add(new Label("pageTitle", "Laywer Demo App"));
	}

}
