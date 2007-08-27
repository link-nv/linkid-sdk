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
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.SupportedAuthenticationProtocol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
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

	public static final String SAML2_POST_BINDING_VM_RESOURCE = "/net/link/safeonline/sdk/auth/saml2/saml2-post-binding.vm";

	public static final String SAML2_BROWSER_POST_TEMPLATE_CONFIG_PARAM = "Saml2BrowserPostTemplate";

	private static final Log LOG = LogFactory
			.getLog(Saml2BrowserPostAuthenticationProtocolHandler.class);

	private String authnServiceUrl;

	private String applicationName;

	private KeyPair applicationKeyPair;

	private Map<String, String> configParams;

	public void init(String authnServiceUrl, String applicationName,
			KeyPair applicationKeyPair, Map<String, String> configParams) {
		LOG.debug("init");
		this.authnServiceUrl = authnServiceUrl + "/entry";
		this.applicationName = applicationName;
		this.applicationKeyPair = applicationKeyPair;
		this.configParams = configParams;
	}

	public void initiateAuthentication(ServletRequest request,
			ServletResponse response, String targetUrl) throws IOException,
			ServletException {
		LOG.debug("target url: " + targetUrl);

		String samlRequestToken = AuthnRequestFactory.createAuthnRequest(
				this.applicationName, this.applicationKeyPair, targetUrl);

		String encodedSamlRequestToken = Base64.encode(samlRequestToken
				.getBytes());

		/*
		 * We could use the opensaml2 HTTPPostEncoderBuilder here to construct
		 * the HTTP response. But this code is just too complex in usage. It's
		 * easier to do all these things ourselves.
		 */
		Properties velocityProperties = new Properties();
		velocityProperties.put("resource.loader", "class");
		velocityProperties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
				Log4JLogChute.class.getName());
		velocityProperties.put(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER,
				Saml2BrowserPostAuthenticationProtocolHandler.class.getName());
		velocityProperties
				.put("class.resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		VelocityEngine velocityEngine;
		try {
			velocityEngine = new VelocityEngine(velocityProperties);
			velocityEngine.init();
		} catch (Exception e) {
			throw new ServletException("could not initialize velocity engine");
		}
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("action", this.authnServiceUrl);
		velocityContext.put("SAMLRequest", encodedSamlRequestToken);

		String templateResourceName;
		if (this.configParams
				.containsKey(SAML2_BROWSER_POST_TEMPLATE_CONFIG_PARAM)) {
			templateResourceName = this.configParams
					.get(SAML2_BROWSER_POST_TEMPLATE_CONFIG_PARAM);
		} else {
			templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;
		}

		Template template;
		try {
			template = velocityEngine.getTemplate(templateResourceName);
		} catch (Exception e) {
			throw new ServletException("Velocity template error: "
					+ e.getMessage(), e);
		}

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		template.merge(velocityContext, out);
	}
}
