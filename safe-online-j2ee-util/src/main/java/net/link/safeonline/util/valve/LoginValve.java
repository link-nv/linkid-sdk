/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.valve;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SimplePrincipal;

public class LoginValve extends ValveBase {

	private static final Log LOG = LogFactory.getLog(LoginValve.class);

	public LoginValve() {
		LOG.debug("login valve construction");
	}

	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {
		LOG.debug("login valve invoked");

		HttpServletRequest httpServletRequest = request.getRequest();

		HttpSession httpSession = httpServletRequest.getSession(false);
		if (null != httpSession) {
			LOG.debug("http session present");
			String username = (String) httpSession.getAttribute("username");
			if (null != username) {
				LOG.debug("setting user principal to " + username);
				request.setUserPrincipal(new SimplePrincipal(username));
			}
		}
		Valve valve = this.getNext();
		if (null != valve) {
			valve.invoke(request, response);
		}
	}
}
