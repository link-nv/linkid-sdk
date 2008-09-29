package net.link.safeonline.demo.lawyer.webapp;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;


public class Layout extends WebPage {

	private static final long serialVersionUID = 1L;

	public Layout() {
		add(new Label("pageTitle", "Laywer Demo App"));
	}

}
