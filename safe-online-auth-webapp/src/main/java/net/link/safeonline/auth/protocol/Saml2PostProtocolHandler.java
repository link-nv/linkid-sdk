/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Server-side protocol handler for the SAML2 Browser POST authentication
 * protocol.
 * 
 * @author fcorneli
 * 
 */
public class Saml2PostProtocolHandler implements ProtocolHandler {

	private static final Log LOG = LogFactory
			.getLog(Saml2PostProtocolHandler.class);

	public String getName() {
		return "SAML v2 Browser POST Authentication Protocol";
	}

	public String handleRequest(HttpServletRequest authnRequest) {
		if ("POST".equals(authnRequest.getMethod())) {
			return null;
		}
		String encodedSamlRequest = authnRequest.getParameter("SAMLRequest");
		if (null != encodedSamlRequest) {
			return null;
		}
		LOG.debug("SAMLRequest parameter found");
		// ...
		return null;
	}
}
