package net.link.safeonline.demo.lawyer.webapp;

import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;

public class Layout extends WebPage {

	private static final long serialVersionUID = 1L;

	public Layout() {
		add(new Label("pageTitle", "Laywer Demo App"));
	}

}
