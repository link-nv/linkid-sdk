package net.link.safeonline.demo.lawyer.webapp;

import net.link.safeonline.demo.wicket.tools.RolesAllowed;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.PageLink;

@RolesAllowed("baradmin")
public class HomePage extends Layout {

	private static final long serialVersionUID = 1L;

	public HomePage() {
		add(new Label("headerTitle", "Welcome Page"));
		add(new AdminNavigationBorder("navigation"));
		add(new PageLink("listLink", ListPage.class));
		add(new PageLink("editLink", FindLawyer.class));
	}

}
