/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Server-side authentication protocol handler for the simple/unsecure
 * authentication protocol.
 * 
 * <p>
 * The protocol request is a simple HTTP GET with parameters 'application' and
 * 'target'.
 * </p>
 * 
 * <p>
 * The protocol response is a simple HTTP redirect with parameter 'username'.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class SimpleProtocolHandler implements ProtocolHandler {

	private static final Log LOG = LogFactory
			.getLog(SimpleProtocolHandler.class);

	public static final String NAME = "Simple Authentication Protocol";

	public ProtocolContext handleRequest(HttpServletRequest authnRequest)
			throws ProtocolException {
		if (false == "GET".equals(authnRequest.getMethod())) {
			return null;
		}
		String applicationId = authnRequest.getParameter("application");
		if (null == applicationId) {
			return null;
		}
		LOG.debug("application: " + applicationId);
		/*
		 * From this moment on we're sure that the user is really trying to use
		 * the simple authentication protocol.
		 */
		String target = authnRequest.getParameter("target");
		if (null == target) {
			/*
			 * The simple authentication protocol really requires the "target"
			 * request parameter.
			 */
			throw new ProtocolException("target request parameter not found");
		}
		LOG.debug("setting target: " + target);
		return new ProtocolContext(applicationId, target);
	}

	public String getName() {
		return NAME;
	}

	public void authnResponse(HttpSession session,
			HttpServletResponse authnResponse) throws ProtocolException {
		String userId = (String) session.getAttribute("username");
		String target = (String) session.getAttribute("target");

		String redirectUrl;
		try {
			redirectUrl = target + "?username="
					+ URLEncoder.encode(userId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException("unsupported encoding: "
					+ e.getMessage());
		}

		LOG.debug("redirecting to: " + redirectUrl);
		try {
			authnResponse.sendRedirect(redirectUrl);
		} catch (IOException e) {
			throw new ProtocolException("IO error: " + e.getMessage());
		}
	}
}
