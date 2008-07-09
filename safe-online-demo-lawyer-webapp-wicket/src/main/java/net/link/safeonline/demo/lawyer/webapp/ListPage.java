package net.link.safeonline.demo.lawyer.webapp;

import net.link.safeonline.demo.wicket.tools.RolesAllowed;

import org.apache.wicket.markup.html.basic.Label;

@RolesAllowed("baradmin")
public class ListPage extends Layout {

	private static final long serialVersionUID = 1L;

	public ListPage() {
		add(new Label<String>("headerTitle", "List of lawyers"));
		add(new AdminNavigationBorder("navigation"));
	}

}
