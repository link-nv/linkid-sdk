/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.saml2;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.DomUtils;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.Challenge;
import net.link.safeonline.sdk.auth.saml2.SamlResponseMessageContext;
import net.link.safeonline.sdk.auth.saml2.SamlResponseSecurityPolicyResolver;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.SecurityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation class for the SAML2 browser POST authentication protocol. This
 * class is used to send out a SAML2 authentication request to an external
 * device provider. And receive a SAML2 reponse from that device provider.
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

	private String applicationName;

	private KeyPair applicationKeyPair;

	private X509Certificate applicationCertificate;

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

		if (null == instance) {
			instance = new Saml2BrowserPostHandler(request);
		}

		return instance;
	}

	public void init(String inAuthnServiceUrl, String inApplicationName,
			KeyPair inApplicationKeyPair,
			X509Certificate inApplicationCertificate,
			Map<String, String> inConfigParams) {
		LOG.debug("init");
		this.authnServiceUrl = inAuthnServiceUrl;
		this.applicationName = inApplicationName;
		this.applicationKeyPair = inApplicationKeyPair;
		this.applicationCertificate = inApplicationCertificate;
		this.configParams = inConfigParams;
		this.challenge = new Challenge<String>();
		this.wsLocation = inConfigParams.get("WsLocation");
	}

	public void authnRequest(@SuppressWarnings("unused")
	HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			String targetUrl, String device) throws IOException,
			ServletException {
		LOG.debug("target url: " + targetUrl);
		Set<String> devices = new HashSet<String>();
		devices.add(device);
		String samlRequestToken = AuthnRequestFactory.createAuthnRequest(
				this.applicationName, this.applicationKeyPair,
				this.authnServiceUrl, targetUrl, this.challenge, devices);

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
				Saml2BrowserPostHandler.class.getName());
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
		velocityContext.put("action", targetUrl);
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

		httpResponse.setContentType("text/html");
		PrintWriter out = httpResponse.getWriter();
		template.merge(velocityContext, out);
	}

	public String handleResponse(HttpServletRequest httpRequest,
			@SuppressWarnings("unused")
			HttpServletResponse httpResponse) throws ServletException {

		DateTime now = new DateTime();

		if (false == "POST".equals(httpRequest.getMethod())) {
			return null;
		}
		LOG.debug("POST request");
		String encodedSamlResponse = httpRequest.getParameter("SAMLResponse");
		if (null == encodedSamlResponse) {
			LOG.debug("no SAMLResponse parameter found");
			return null;
		}
		LOG.debug("SAMLResponse parameter found");
		LOG.debug("encodedSamlResponse: " + encodedSamlResponse);

		String expectedInResponseTo = this.challenge.getValue();
		SamlResponseMessageContext messageContext = new SamlResponseMessageContext(
				expectedInResponseTo, this.applicationName);
		messageContext
				.setInboundMessageTransport(new HttpServletRequestAdapter(
						httpRequest));

		SecurityPolicyResolver securityPolicyResolver = new SamlResponseSecurityPolicyResolver();
		messageContext.setSecurityPolicyResolver(securityPolicyResolver);

		HTTPPostDecoder decoder = new HTTPPostDecoder();
		try {
			decoder.decode(messageContext);
		} catch (MessageDecodingException e) {
			LOG.debug("SAML message decoding error: " + e.getMessage(), e);
			throw new ServletException("SAML message decoding error");
		} catch (SecurityPolicyException e) {
			LOG.debug("security policy error: " + e.getMessage(), e);
			throw new ServletException("security policy error");
		} catch (SecurityException e) {
			LOG.debug("security error: " + e.getMessage(), e);
			throw new ServletException("security error");
		}

		SAMLObject samlMessage = messageContext.getInboundSAMLMessage();
		if (false == samlMessage instanceof Response) {
			throw new ServletException("SAML message not an response message");
		}
		Response samlResponse = (Response) samlMessage;

		if (null != this.wsLocation) {
			byte[] decodedSamlResponse;
			try {
				decodedSamlResponse = Base64.decode(encodedSamlResponse);
			} catch (Base64DecodingException e) {
				throw new ServletException("BASE64 decoding error");
			}
			Document samlDocument;
			try {
				samlDocument = DomUtils.parseDocument(new String(
						decodedSamlResponse));
			} catch (Exception e) {
				throw new ServletException("DOM parsing error");
			}
			Element samlElement = samlDocument.getDocumentElement();
			SecurityTokenServiceClient stsClient = new SecurityTokenServiceClientImpl(
					this.wsLocation, this.applicationCertificate,
					this.applicationKeyPair.getPrivate());
			try {
				stsClient.validate(samlElement);
			} catch (RuntimeException e) {
				throw new ServletException(e.getMessage());
			}
		}

		List<Assertion> assertions = samlResponse.getAssertions();
		if (assertions.isEmpty()) {
			throw new ServletException("missing Assertion");
		}

		Assertion assertion = assertions.get(0);
		List<AuthnStatement> authStatements = assertion.getAuthnStatements();
		if (authStatements.isEmpty()) {
			throw new ServletException("missing authentication statement");
		}

		AuthnStatement authStatement = authStatements.get(0);
		if (null == authStatement.getAuthnContext()) {
			throw new ServletException(
					"missing authentication context in authentication statement");
		}

		AuthnContextClassRef authnContextClassRef = authStatement
				.getAuthnContext().getAuthnContextClassRef();
		this.authenticationDevice = authnContextClassRef
				.getAuthnContextClassRef();
		LOG.debug("authentication device: " + this.authenticationDevice);

		Conditions conditions = assertion.getConditions();
		DateTime notBefore = conditions.getNotBefore();
		DateTime notOnOrAfter = conditions.getNotOnOrAfter();
		if (now.isBefore(notBefore) || now.isAfter(notOnOrAfter)) {
			throw new ServletException("invalid SAML message timeframe");
		}

		Subject subject = assertion.getSubject();
		if (null == subject) {
			throw new ServletException("missing Assertion Subject");
		}
		NameID subjectName = subject.getNameID();
		String subjectNameValue = subjectName.getValue();
		LOG.debug("subject name value: " + subjectNameValue);
		return subjectNameValue;
	}

	public String getAuthenticationDevice() {
		return this.authenticationDevice;
	}
}
