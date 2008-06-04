package net.link.safeonline.demo.lawyer.webapp;

import net.link.safeonline.demo.wicket.tools.RolesAllowed;
import org.apache.wicket.markup.html.basic.Label;

@RolesAllowed
public class ViewProfile extends Layout {

	private static final long serialVersionUID = 1L;

	public ViewProfile() {
		add(new Label("headerTitle", "Your Profile"));
	}

}
