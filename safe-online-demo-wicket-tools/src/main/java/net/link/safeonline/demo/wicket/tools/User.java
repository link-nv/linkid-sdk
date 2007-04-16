package net.link.safeonline.demo.wicket.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private String username;

	private List<String> roles = new ArrayList<String>();

	public User() {
		// empty
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean hasOneOf(List<String> roles) {
		if (roles == null)
			return true;
		if (roles.size() == 0)
			return true;

		for (String hasRole : this.roles) {
			for (String needRole : roles) {
				if (hasRole.equals(needRole)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean has(String role) {
		for (String hasRole : roles) {
			if (hasRole.equals(role)) {
				return true;
			}
		}
		return false;
	}

}