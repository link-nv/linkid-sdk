/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Server-side authentication protocol handler for the simple authentication
 * protocol.
 * 
 * @author fcorneli
 * 
 */
public class SimpleProtocolHandler implements ProtocolHandler {

	private static final Log LOG = LogFactory
			.getLog(SimpleProtocolHandler.class);

	public String handleRequest(HttpServletRequest authnRequest) {
		if (false == "GET".equals(authnRequest.getMethod())) {
			return null;
		}
		String applicationId = authnRequest.getParameter("application");
		if (null == applicationId) {
			return null;
		}
		LOG.debug("application: " + applicationId);
		String target = authnRequest.getParameter("target");
		if (null != target) {
			HttpSession session = authnRequest.getSession();
			LOG.debug("setting target: " + target);
			session.setAttribute("target", target);
		}
		return applicationId;
	}

	public String getName() {
		return "Simple Authentication Protocol";
	}
}
