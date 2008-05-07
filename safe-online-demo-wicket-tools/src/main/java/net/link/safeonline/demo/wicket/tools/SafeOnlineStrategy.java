package net.link.safeonline.demo.wicket.tools;

import java.util.Arrays;
import java.util.List;

import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import wicket.Component;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.authorization.Action;
import wicket.authorization.IAuthorizationStrategy;

public class SafeOnlineStrategy implements IAuthorizationStrategy {

	private Class<?> loginPageClass = null;

	public SafeOnlineStrategy(Class<?> loginPageClass) {
		this.loginPageClass = loginPageClass;
	}

	public boolean isActionAuthorized(Component arg0, Action arg1) {
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean isInstantiationAuthorized(final Class page) {
		final RolesAllowed roles = (RolesAllowed) page
				.getAnnotation(RolesAllowed.class);
		if (roles == null) {
			System.out.println("no annotation found");
			return true;
		}

		// check if username is in session
		HttpServletRequest httpServletRequest;
		try {
			httpServletRequest = (HttpServletRequest) PolicyContext
					.getContext("javax.servlet.http.HttpServletRequest");
		} catch (PolicyContextException e) {
			return false;
		}
		HttpSession session = httpServletRequest.getSession();
		String username = (String) session.getAttribute("username");
		if (username == null) {
			throw new RestartResponseAtInterceptPageException(
					this.loginPageClass);
		}

		// find user in session
		User user = getUser();
		if (user == null || !user.getUsername().equals(username)) {
			user = new User();
			user.setUsername(username);
			setUser(user);
		}

		// check if roles match
		List<String> needRoles = Arrays.asList(roles.value());
		if (!user.hasOneOf(needRoles)) {
			return false;
		}
		return true;
	}

	protected User getUser() {
		RoleSession session = (RoleSession) Session.get();
		return session.getUser();
	}

	private void setUser(User user) {
		RoleSession session = (RoleSession) Session.get();
		session.setUser(user);
	}

}
