package net.link.safeonline.demo.wicket.tools;

import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebSession;

public class RoleSession extends WebSession {

	private static final long serialVersionUID = 1L;

	private User user;

	public RoleSession(WebApplication application) {
		super(application);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
