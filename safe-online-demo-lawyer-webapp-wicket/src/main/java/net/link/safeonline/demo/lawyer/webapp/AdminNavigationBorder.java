package net.link.safeonline.demo.lawyer.webapp;

import net.link.safeonline.demo.wicket.tools.RolesAllowed;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.link.PageLink;

@RolesAllowed("baradmin")
public class AdminNavigationBorder extends Border {

	private static final long serialVersionUID = 1L;

	public AdminNavigationBorder(String componentName) {
		super(componentName);
		add(new PageLink("homelink", HomePage.class));
		add(new PageLink("listlink", ListPage.class));
		add(new PageLink("editlink", FindLawyer.class));
	}

}
