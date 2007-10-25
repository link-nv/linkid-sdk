/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sts.ws;

import java.security.PublicKey;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.TransformerException;

import net.link.safeonline.util.ee.IdentityServiceClient;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.joda.time.DateTime;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ValidateTargetType;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Element;

/**
 * Implementation of WS-Trust 1.3 STS JAX-WS web service endpoint.
 * 
 * @author fcorneli
 */
@WebService(endpointInterface = "org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort")
@HandlerChain(file = "app-auth-ws-handlers.xml")
@Injection
public class SecurityTokenServicePortImpl implements SecurityTokenServicePort {

	private static final Log LOG = LogFactory
			.getLog(SecurityTokenServicePortImpl.class);

	private PublicKey publicKey;

	@PostConstruct
	public void postConstructCallback() {
		LOG.debug("post construct");
		IdentityServiceClient identityServiceClient = new IdentityServiceClient();
		this.publicKey = identityServiceClient.getPublicKey();
	}

	public RequestSecurityTokenResponseType requestSecurityToken(
			RequestSecurityTokenType request) {
		LOG.debug("request security token");
		String requestType = null;
		ValidateTargetType validateTarget = null;
		List<Object> content = request.getAny();
		for (Object contentObject : content) {
			if (contentObject instanceof JAXBElement) {
				JAXBElement<?> contentElement = (JAXBElement<?>) contentObject;
				Object value = contentElement.getValue();
				if (value instanceof String) {
					requestType = (String) value;
				} else if (value instanceof ValidateTargetType) {
					validateTarget = (ValidateTargetType) value;
				}
			}
		}
		if (null == requestType) {
			throw new RuntimeException("RequestType is required");
		}
		if (false == "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate"
				.equals(requestType)) {
			throw new RuntimeException("only supporting the validation binding");
		}
		if (null == validateTarget) {
			throw new RuntimeException("ValidateTarget is required");
		}
		Element tokenElement = (Element) validateTarget.getAny();
		if (null == tokenElement) {
			throw new RuntimeException("missing token to validate");
		}

		Element nsElement = tokenElement.getOwnerDocument().createElement(
				"nsElement");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds",
				"http://www.w3.org/2000/09/xmldsig#");

		Element signatureElement;
		try {
			signatureElement = (Element) XPathAPI.selectSingleNode(
					tokenElement, "ds:Signature", nsElement);
		} catch (TransformerException e) {
			throw new RuntimeException("XPath error");
		}
		try {
			XMLSignature xmlSignature = new XMLSignature(signatureElement, null);
			boolean result = xmlSignature.checkSignatureValue(this.publicKey);
			LOG.debug("result of XML Signature validation: " + result);
		} catch (XMLSecurityException e) {
			throw new RuntimeException("XML security error");
		}

		Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory()
				.getUnmarshaller(tokenElement);
		XMLObject tokenXmlObject;
		try {
			tokenXmlObject = unmarshaller.unmarshall(tokenElement);
		} catch (UnmarshallingException e) {
			LOG.debug("error parsing token: " + e.getMessage(), e);
			throw new RuntimeException("error parsing token");
		}
		if (false == tokenXmlObject instanceof Response) {
			throw new RuntimeException("token not a SAML2 Response");
		}
		Response samlResponse = (Response) tokenXmlObject;
		List<Assertion> assertions = samlResponse.getAssertions();
		if (assertions.isEmpty()) {
			throw new RuntimeException("missing Assertion in SAML2 Response");
		}
		Assertion assertion = assertions.get(0);

		Conditions conditions = assertion.getConditions();
		DateTime notBefore = conditions.getNotBefore();
		DateTime notOnOrAfter = conditions.getNotOnOrAfter();
		DateTime now = new DateTime();
		if (now.isBefore(notBefore) || now.isAfter(notOnOrAfter)) {
			throw new RuntimeException("invalid SAML message timeframe");
		}

		Subject subject = assertion.getSubject();
		if (null == subject) {
			throw new RuntimeException("missing Assertion Subject");
		}
		NameID subjectName = subject.getNameID();
		String subjectNameValue = subjectName.getValue();
		LOG.debug("subject name value: " + subjectNameValue);

		BasicX509Credential basicX509Credential = new BasicX509Credential();
		basicX509Credential.setPublicKey(this.publicKey);
		SignatureValidator signatureValidator = new SignatureValidator(
				basicX509Credential);
		Signature signature = samlResponse.getSignature();
		if (null == signature) {
			throw new RuntimeException("SAML token has no Signature element");
		}
		try {
			signatureValidator.validate(samlResponse.getSignature());
		} catch (ValidationException e) {
			LOG.error("validation error: " + e.getMessage(), e);
			throw new RuntimeException(
					"SAML2 response token signature not valid");
		}

		RequestSecurityTokenResponseType response = new RequestSecurityTokenResponseType();
		return response;
	}
}
