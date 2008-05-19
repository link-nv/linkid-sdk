/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.auth.saml2;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestUtil;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseUtil;
import net.link.safeonline.sdk.auth.saml2.Challenge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.ConfigurationException;

/**
 * Implementation class for the SAML2 browser POST authentication protocol. This
 * class is used to send out a SAML2 authentication request to an external
 * device provider. And receive a SAML2 reponse from that device provider which
 * should contain the device mapping id.
 * 
 * <p>
 * Optional configuration parameters:
 * </p>
 * <ul>
 * <li><code>Saml2BrowserPostTemplate</code>: contains the path to the
 * custom SAML2 Browser POST template resource.</li>
 * <li><code>WsLocation</code>: contains the location of the OLAS web
 * services. If present this handler will use the STS web service for SAML
 * authentication token validation.</li>
 * </ul>
 * 
 * @author wvdhaute
 * 
 */
public class Saml2BrowserPostHandler implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String SAML2_BROWSER_POST_HANDLER = Saml2BrowserPostHandler.class
			.getName()
			+ ".SAML2_BROWSER_POST_HANDLER";

	public static final String SAML2_POST_BINDING_VM_RESOURCE = "/net/link/safeonline/device/sdk/saml2/saml2-post-binding.vm";

	public static final String SAML2_BROWSER_POST_TEMPLATE_CONFIG_PARAM = "Saml2BrowserPostTemplate";

	private static final Log LOG = LogFactory
			.getLog(Saml2BrowserPostHandler.class);

	static {
		/*
		 * Next is because Sun loves to endorse crippled versions of Xerces.
		 */
		System
				.setProperty(
						"javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
						"org.apache.xerces.jaxp.validation.XMLSchemaFactory");
		try {
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			throw new RuntimeException(
					"could not bootstrap the OpenSAML2 library");
		}
	}

	private HttpSession session;

	private String authnServiceUrl;

	private String issuerName;

	private String applicationName;

	private KeyPair applicationKeyPair;

	private Map<String, String> configParams;

	private Challenge<String> challenge;

	private String wsLocation;

	private String authenticationDevice;

	private Saml2BrowserPostHandler(HttpServletRequest request) {
		this.session = request.getSession();
		this.session.setAttribute(SAML2_BROWSER_POST_HANDLER, this);
	}

	public static Saml2BrowserPostHandler getSaml2BrowserPostHandler(
			HttpServletRequest request) {
		Saml2BrowserPostHandler instance = (Saml2BrowserPostHandler) request
				.getSession().getAttribute(SAML2_BROWSER_POST_HANDLER);

		if (null == instance)
			instance = new Saml2BrowserPostHandler(request);

		return instance;
	}

	public void init(String inAuthnServiceUrl, String inIssuerName,
			String inApplicationName, KeyPair inApplicationKeyPair,
			Map<String, String> inConfigParams) {
		LOG.debug("init");
		this.authnServiceUrl = inAuthnServiceUrl;
		this.issuerName = inIssuerName;
		this.applicationName = inApplicationName;
		this.applicationKeyPair = inApplicationKeyPair;
		this.configParams = inConfigParams;
		this.challenge = new Challenge<String>();
		this.wsLocation = inConfigParams.get("WsLocation");
	}

	public void authnRequest(
			@SuppressWarnings("unused") HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, String targetUrl, String device)
			throws IOException, ServletException {
		LOG.debug("target url: " + targetUrl);
		Set<String> devices = new HashSet<String>();
		devices.add(device);
		String samlRequestToken = AuthnRequestFactory.createAuthnRequest(
				this.issuerName, this.applicationName, this.applicationKeyPair,
				this.authnServiceUrl, targetUrl, this.challenge, devices);

		String encodedSamlRequestToken = Base64.encode(samlRequestToken
				.getBytes());

		String templateResourceName;
		if (this.configParams
				.containsKey(SAML2_BROWSER_POST_TEMPLATE_CONFIG_PARAM))
			templateResourceName = this.configParams
					.get(SAML2_BROWSER_POST_TEMPLATE_CONFIG_PARAM);
		else
			templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;

		AuthnRequestUtil.sendAuthnRequest(targetUrl, encodedSamlRequestToken,
				templateResourceName, httpResponse);
	}

	public String handleResponse(HttpServletRequest httpRequest,
			X509Certificate certificate, PrivateKey privateKey)
			throws ServletException {

		DateTime now = new DateTime();

		Response samlResponse = AuthnResponseUtil.validateResponse(now,
				httpRequest, this.challenge.getValue(), this.applicationName,
				this.wsLocation, certificate, privateKey);
		if (null == samlResponse)
			return null;

		Assertion assertion = samlResponse.getAssertions().get(0);
		List<AuthnStatement> authStatements = assertion.getAuthnStatements();
		if (authStatements.isEmpty())
			throw new ServletException("missing authentication statement");

		AuthnStatement authStatement = authStatements.get(0);
		if (null == authStatement.getAuthnContext())
			throw new ServletException(
					"missing authentication context in authentication statement");

		AuthnContextClassRef authnContextClassRef = authStatement
				.getAuthnContext().getAuthnContextClassRef();
		this.authenticationDevice = authnContextClassRef
				.getAuthnContextClassRef();
		LOG.debug("authentication device: " + this.authenticationDevice);

		Subject subject = assertion.getSubject();
		NameID subjectName = subject.getNameID();
		String subjectNameValue = subjectName.getValue();
		LOG.debug("subject name value: " + subjectNameValue);
		return subjectNameValue;
	}

	public String getAuthenticationDevice() {
		return this.authenticationDevice;
	}
}
