package net.link.safeonline.demo.cinema.webapp;

import net.link.safeonline.demo.wicket.tools.RolesAllowed;

import org.apache.wicket.markup.html.basic.Label;

@RolesAllowed("baradmin")
public class HomePage extends Layout {

	private static final long serialVersionUID = 1L;

	public HomePage() {
		add(new Label<String>("headerTitle", "Welcome Page"));
        // add(new AdminNavigationBorder("navigation"));
        // add(new PageLink("listLink", ListPage.class));
        // add(new PageLink("editLink", FindCinema.class));
	}

}
