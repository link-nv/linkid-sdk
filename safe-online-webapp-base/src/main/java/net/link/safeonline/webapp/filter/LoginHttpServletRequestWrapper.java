/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.filter;

import java.security.Principal;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Login HTTP Servlet Request Wrapper. This wrapper adds user principal and
 * roles to the request.
 * 
 * @author fcorneli
 * 
 */
public class LoginHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private final Principal userPrincipal;

	private final Set<String> roles;

	public LoginHttpServletRequestWrapper(HttpServletRequest request,
			Principal userPrincipal, Set<String> roles) {
		super(request);

		this.userPrincipal = userPrincipal;
		this.roles = roles;
	}

	@Override
	public Principal getUserPrincipal() {
		return this.userPrincipal;
	}

	@Override
	public boolean isUserInRole(String role) {
		boolean userInRole = this.roles.contains(role);
		return userInRole;
	}
}