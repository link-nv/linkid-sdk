/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyPair;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.SupportedAuthenticationProtocol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;

/**
 * Implementation class for the SAML2 browser POST authentication protocol.
 * 
 * @author fcorneli
 * 
 */
@SupportedAuthenticationProtocol(AuthenticationProtocol.SAML2_BROWSER_POST)
public class Saml2BrowserPostAuthenticationProtocolHandler implements
		AuthenticationProtocolHandler {

	private static final Log LOG = LogFactory
			.getLog(Saml2BrowserPostAuthenticationProtocolHandler.class);

	private String authnServiceUrl;

	private String applicationName;

	private KeyPair applicationKeyPair;

	public void init(String authnServiceUrl, String applicationName,
			KeyPair applicationKeyPair) {
		LOG.debug("init");
		this.authnServiceUrl = authnServiceUrl;
		this.applicationName = applicationName;
		this.applicationKeyPair = applicationKeyPair;
	}

	public void initiateAuthentication(ServletRequest request,
			ServletResponse response, String targetUrl) throws IOException,
			ServletException {
		LOG.debug("target url: " + targetUrl);

		String samlRequestToken = AuthnRequestFactory.createAuthnRequest(
				this.applicationName, this.applicationKeyPair);

		String encodedSamlRequestToken = Base64.encode(samlRequestToken
				.getBytes());

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<body onload=\"document.postForm.submit();\">");
		out.println("<h1>SAML2 Authentication Request Post</h1>");
		out
				.println("<p>Please wait while you're being redirected to the authentication application...</p>");
		out.println("<form name=\"postForm\" action=\"" + this.authnServiceUrl
				+ "\" method=\"POST\">");
		out.println("<input type=\"hidden\" name=\"SAMLRequest\" value=\""
				+ encodedSamlRequestToken + "\" />");
		out.println("</form>");
		out.println("</body>");
		out.println("</html>");
		out.close();
	}
}
