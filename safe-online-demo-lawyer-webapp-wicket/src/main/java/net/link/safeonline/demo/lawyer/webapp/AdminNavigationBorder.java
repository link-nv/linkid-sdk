package net.link.safeonline.demo.lawyer.webapp;

import net.link.safeonline.demo.wicket.tools.RolesAllowed;

import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.link.PageLink;

@RolesAllowed("baradmin")
public class AdminNavigationBorder extends Border<String> {

	private static final long serialVersionUID = 1L;

	public AdminNavigationBorder(String componentName) {
		super(componentName);
		add(new PageLink<String>("homelink", HomePage.class));
        add(new PageLink<String>("listlink", ListPage.class));
        add(new PageLink<String>("editlink", FindLawyer.class));
	}

}
