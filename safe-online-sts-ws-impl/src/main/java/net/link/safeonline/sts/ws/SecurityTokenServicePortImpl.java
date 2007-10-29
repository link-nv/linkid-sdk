/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sts.ws;

import java.util.List;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceContext;

import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ObjectFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort;
import org.oasis_open.docs.ws_sx.ws_trust._200512.StatusType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ValidateTargetType;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Element;

/**
 * Implementation of WS-Trust 1.3 STS JAX-WS web service endpoint. Beware that
 * we validate both the WS-Security and SAML token signature via SOAP handlers.
 * The signature validation cannot be done within the endpoint implementation
 * since JAXB somehow breaks the signature digests.
 * 
 * @author fcorneli
 */
@WebService(endpointInterface = "org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort")
@HandlerChain(file = "sts-ws-handlers.xml")
@Injection
public class SecurityTokenServicePortImpl implements SecurityTokenServicePort {

	private static final Log LOG = LogFactory
			.getLog(SecurityTokenServicePortImpl.class);

	@Resource
	private WebServiceContext context;

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
			RequestSecurityTokenResponseType response = createResponse(
					SecurityTokenServiceConstants.STATUS_INVALID,
					"ValidateTarget is required");
			return response;
		}
		Element tokenElement = (Element) validateTarget.getAny();
		if (null == tokenElement) {
			RequestSecurityTokenResponseType response = createResponse(
					SecurityTokenServiceConstants.STATUS_INVALID,
					"missing token to validate");
			return response;
		}

		boolean result = TokenValidationHandler.getValidity(this.context);
		if (false == result) {
			LOG.debug("token signature not valid");
			RequestSecurityTokenResponseType response = createResponse(
					SecurityTokenServiceConstants.STATUS_INVALID,
					"token signature not valid");
			return response;
		}

		Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory()
				.getUnmarshaller(tokenElement);
		XMLObject tokenXmlObject;
		try {
			tokenXmlObject = unmarshaller.unmarshall(tokenElement);
		} catch (UnmarshallingException e) {
			LOG.debug("error parsing token: " + e.getMessage(), e);
			RequestSecurityTokenResponseType response = createResponse(
					SecurityTokenServiceConstants.STATUS_INVALID,
					"error parsing token");
			return response;
		}
		if (false == tokenXmlObject instanceof Response) {
			RequestSecurityTokenResponseType response = createResponse(
					SecurityTokenServiceConstants.STATUS_INVALID,
					"token not a SAML2 Response");
			return response;
		}
		Response samlResponse = (Response) tokenXmlObject;

		String samlStatusCode = samlResponse.getStatus().getStatusCode()
				.getValue();
		if (false == StatusCode.SUCCESS_URI.equals(samlStatusCode)) {
			LOG.debug("SAML status code: " + samlStatusCode);
			RequestSecurityTokenResponseType response = createResponse(
					SecurityTokenServiceConstants.STATUS_INVALID,
					"invalid SAML2 token status code");
			return response;
		}

		List<Assertion> assertions = samlResponse.getAssertions();
		if (assertions.isEmpty()) {
			RequestSecurityTokenResponseType response = createResponse(
					SecurityTokenServiceConstants.STATUS_INVALID,
					"missing Assertion in SAML2 Response");
			return response;
		}
		Assertion assertion = assertions.get(0);

		Conditions conditions = assertion.getConditions();
		DateTime notBefore = conditions.getNotBefore();
		DateTime notOnOrAfter = conditions.getNotOnOrAfter();
		DateTime now = new DateTime();
		if (now.isBefore(notBefore) || now.isAfter(notOnOrAfter)) {
			RequestSecurityTokenResponseType response = createResponse(
					SecurityTokenServiceConstants.STATUS_INVALID,
					"invalid SAML message timeframe");
			return response;
		}

		Subject subject = assertion.getSubject();
		if (null == subject) {
			RequestSecurityTokenResponseType response = createResponse(
					SecurityTokenServiceConstants.STATUS_INVALID,
					"missing Assertion Subject");
			return response;
		}
		NameID subjectName = subject.getNameID();
		String subjectNameValue = subjectName.getValue();
		LOG.debug("subject name value: " + subjectNameValue);

		List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
		if (authnStatements.isEmpty()) {
			RequestSecurityTokenResponseType response = createResponse(
					SecurityTokenServiceConstants.STATUS_INVALID,
					"no authentication statement present");
			return response;
		}
		AuthnStatement authnStatement = authnStatements.get(0);
		AuthnContextClassRef authnContextClassRef = authnStatement
				.getAuthnContext().getAuthnContextClassRef();
		String authnDevice = authnContextClassRef.getAuthnContextClassRef();
		LOG.debug("authentication device: " + authnDevice);

		RequestSecurityTokenResponseType response = createResponse(
				SecurityTokenServiceConstants.STATUS_VALID, null);
		return response;
	}

	private RequestSecurityTokenResponseType createResponse(String statusCode,
			String reason) {
		ObjectFactory objectFactory = new ObjectFactory();
		RequestSecurityTokenResponseType response = new RequestSecurityTokenResponseType();
		StatusType status = objectFactory.createStatusType();
		status.setCode(statusCode);
		if (null != reason) {
			status.setReason(reason);
		}
		response.getAny().add(objectFactory.createStatus(status));
		return response;
	}
}
