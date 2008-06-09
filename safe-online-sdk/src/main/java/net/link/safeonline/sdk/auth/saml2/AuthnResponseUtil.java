/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

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
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.security.SecurityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class for SAML2 authentication responses.
 * 
 * @author wvdhaute
 * 
 */
public class AuthnResponseUtil {

	private static final Log LOG = LogFactory.getLog(AuthnResponseUtil.class);

	private AuthnResponseUtil() {
		// empty
	}

	/**
	 * Sends out a SAML reponse message to the specified consumer URL.
	 * 
	 * @param encodedSamlResponseToken
	 * @param consumerUrl
	 * @param publicKey
	 * @param privateKey
	 * @param httpResponse
	 * @throws ServletException
	 * @throws IOException
	 */
	public static void sendAuthnResponse(String encodedSamlResponseToken,
			String templateResourceName, String consumerUrl,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
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
				AuthnResponseUtil.class.getName());
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
		velocityContext.put("action", consumerUrl);
		velocityContext.put("SAMLResponse", encodedSamlResponseToken);

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

	/**
	 * Validates a SAML response in the specified HTTP request. Checks:
	 * <ul>
	 * <li>response ID</li>
	 * <li>response validated with STS WS location</li>
	 * <li>at least 1 assertion present</li>
	 * <li>assertion subject</li>
	 * <li>assertion conditions notOnOrAfter and notBefore
	 * </ul>
	 * 
	 * @param now
	 * @param httpRequest
	 * @param expectedInResponseTo
	 * @param applicationName
	 * @param stsWsLocation
	 * @param applicationCertificate
	 * @param applicationPrivateKey
	 * @throws ServletException
	 */
	public static Response validateResponse(DateTime now,
			HttpServletRequest httpRequest, String expectedInResponseTo,
			String applicationName, String stsWsLocation,
			X509Certificate applicationCertificate,
			PrivateKey applicationPrivateKey, TrustDomainType trustDomain)
			throws ServletException {
		if (false == "POST".equals(httpRequest.getMethod()))
			return null;
		LOG.debug("POST response");
		String encodedSamlResponse = httpRequest.getParameter("SAMLResponse");
		if (null == encodedSamlResponse) {
			LOG.debug("no SAMLResponse parameter found");
			return null;
		}
		LOG.debug("SAMLResponse parameter found");
		LOG.debug("encodedSamlResponse: " + encodedSamlResponse);

		SamlResponseMessageContext messageContext = new SamlResponseMessageContext(
				expectedInResponseTo, applicationName);
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
		if (false == samlMessage instanceof Response)
			throw new ServletException("SAML message not an response message");
		Response samlResponse = (Response) samlMessage;

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
				stsWsLocation, applicationCertificate, applicationPrivateKey);
		stsClient.validate(samlElement, trustDomain);

		List<Assertion> assertions = samlResponse.getAssertions();
		if (assertions.isEmpty())
			throw new ServletException("missing Assertion");

		for (Assertion assertion : assertions) {
			Conditions conditions = assertion.getConditions();
			DateTime notBefore = conditions.getNotBefore();
			DateTime notOnOrAfter = conditions.getNotOnOrAfter();

			LOG.debug("now: " + now.toString());
			LOG.debug("notBefore: " + notBefore.toString());
			LOG.debug("notOnOrAfter : " + notOnOrAfter.toString());

			if (now.isBefore(notBefore) || now.isAfter(notOnOrAfter))
				throw new ServletException("invalid SAML message timeframe");

			Subject subject = assertion.getSubject();
			if (null == subject)
				throw new ServletException("missing Assertion Subject");
		}
		return samlResponse;
	}
}
