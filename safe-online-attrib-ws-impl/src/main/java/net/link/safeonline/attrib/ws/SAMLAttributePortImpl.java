/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attrib.ws;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.ws.common.WebServiceConstants;
import net.link.safeonline.ws.util.ri.Injection;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.assertion.ObjectFactory;
import oasis.names.tc.saml._2_0.assertion.StatementAbstractType;
import oasis.names.tc.saml._2_0.assertion.SubjectType;
import oasis.names.tc.saml._2_0.protocol.AttributeQueryType;
import oasis.names.tc.saml._2_0.protocol.ResponseType;
import oasis.names.tc.saml._2_0.protocol.SAMLAttributePort;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of SAML attribute web service using JAX-WS.
 * <p>
 * Specification: Assertions and Protocols for the OASIS Security Assertion
 * Markup Language (SAML) V2.0.
 * </p>
 * 
 * <p>
 * OASIS Standard, 15 March 2005
 * </p>
 * 
 * <p>
 * SafeOnline extensions: we communicate the multivalued property of an
 * attribute via the {@link AttributeServiceConstants#MULTIVALUED_ATTRIBUTE} XML
 * attribute on the SAML XML "Attribute" element.
 * </p>
 * 
 * @author fcorneli
 * 
 */
@WebService(endpointInterface = "oasis.names.tc.saml._2_0.protocol.SAMLAttributePort")
@HandlerChain(file = "app-auth-ws-handlers.xml")
@Injection
public class SAMLAttributePortImpl implements SAMLAttributePort {

	private static final Log LOG = LogFactory
			.getLog(SAMLAttributePortImpl.class);

	@EJB(mappedName = "SafeOnline/AttributeServiceBean/local")
	private AttributeService attributeService;

	@EJB(mappedName = "SafeOnline/SamlAuthorityServiceBean/local")
	private SamlAuthorityService samlAuthorityService;

	private DatatypeFactory datatypeFactory;

	@PostConstruct
	public void postConstructCallback() {
		try {
			this.datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new EJBException("datatype config error");
		}

		LOG.debug("ready");
	}

	private String findSubjectLogin(AttributeQueryType request) {
		if (null == request) {
			return null;
		}
		SubjectType subject = request.getSubject();
		List<JAXBElement<?>> subjectContent = subject.getContent();
		for (JAXBElement subjectItem : subjectContent) {
			Object value = subjectItem.getValue();
			if (false == value instanceof NameIDType) {
				continue;
			}
			NameIDType nameId = (NameIDType) value;
			String subjectLogin = nameId.getValue();
			return subjectLogin;
		}
		return null;
	}

	public ResponseType attributeQuery(AttributeQueryType request) {
		LOG.debug("attribute query");
		String subjectLogin = findSubjectLogin(request);
		if (null == subjectLogin) {
			LOG.debug("no subject login");
			ResponseType errorResponse = createRequesterErrorResponse(null,
					"no subject found");
			return errorResponse;
		}
		LOG.debug("subject login: " + subjectLogin);

		List<AttributeType> attributes = request.getAttribute();
		Map<String, Object> attributeMap = new HashMap<String, Object>();
		if (0 == attributes.size()) {
			try {
				attributeMap = this.attributeService
						.getConfirmedAttributeValues(subjectLogin);
			} catch (SubjectNotFoundException e) {
				ResponseType subjectNotFoundResponse = createUnknownPrincipalResponse(subjectLogin);
				return subjectNotFoundResponse;
			} catch (PermissionDeniedException e) {
				ResponseType requestDeniedResponse = createRequestDeniedResponse();
				return requestDeniedResponse;
			}
		} else {
			for (AttributeType attribute : attributes) {
				String attributeName = attribute.getName();
				try {
					Object attributeValue = this.attributeService
							.getConfirmedAttributeValue(subjectLogin,
									attributeName);
					attributeMap.put(attributeName, attributeValue);
				} catch (AttributeNotFoundException e) {
					LOG.error("attribute not found: " + attributeName
							+ " for subject " + subjectLogin);
					ResponseType attributeNotFoundResponse = createAttributeNotFoundResponse(attributeName);
					return attributeNotFoundResponse;
				} catch (SubjectNotFoundException e) {
					ResponseType subjectNotFoundResponse = createUnknownPrincipalResponse(subjectLogin);
					return subjectNotFoundResponse;
				} catch (PermissionDeniedException e) {
					ResponseType requestDeniedResponse = createRequestDeniedResponse();
					return requestDeniedResponse;
				}
			}
		}

		ResponseType response = createGenericResponse(SamlpTopLevelErrorCode.SUCCESS);
		List<Object> assertions = response.getAssertionOrEncryptedAssertion();
		AssertionType assertion = getAttributeAssertion(subjectLogin,
				attributeMap);
		assertions.add(assertion);

		return response;
	}

	private ResponseType createGenericResponse(SamlpTopLevelErrorCode errorCode) {
		ResponseType response = new ResponseType();
		String responseId = "urn:uuid:" + UUID.randomUUID().toString();
		response.setID(responseId);
		response.setVersion("2.0");
		XMLGregorianCalendar currentXmlGregorianCalendar = getCurrentXmlGregorianCalendar();
		response.setIssueInstant(currentXmlGregorianCalendar);
		StatusCodeType statusCode = new StatusCodeType();
		statusCode.setValue(errorCode.toString());
		StatusType status = new StatusType();
		status.setStatusCode(statusCode);
		response.setStatus(status);
		return response;
	}

	private enum SamlpTopLevelErrorCode {
		SUCCESS("urn:oasis:names:tc:SAML:2.0:status:Success"), REQUESTER(
				"urn:oasis:names:tc:SAML:2.0:status:Requester"), RESPONDER(
				"urn:oasis:names:tc:SAML:2.0:status:Responder"), VERSION_MISMATCH(
				"urn:oasis:names:tc:SAML:2.0:status:VersionMismatch");

		private final String errorCode;

		private SamlpTopLevelErrorCode(String errorCode) {
			this.errorCode = errorCode;
		}

		@Override
		public String toString() {
			return this.errorCode;
		}
	}

	/**
	 * @param attributeName
	 *            the optional attribute name.
	 * @return
	 */
	private ResponseType createAttributeNotFoundResponse(String attributeName) {
		String detailMessage;
		if (null == attributeName) {
			detailMessage = "Attribute not found.";
		} else {
			detailMessage = "Attribute not found: " + attributeName;
		}
		ResponseType response = createRequesterErrorResponse(
				"urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue",
				detailMessage);
		return response;
	}

	private ResponseType createUnknownPrincipalResponse(String subjectLogin) {
		ResponseType response = createRequesterErrorResponse(
				"urn:oasis:names:tc:SAML:2.0:status:UnknownPrincipal",
				"Subject not found: " + subjectLogin);
		return response;
	}

	private ResponseType createRequestDeniedResponse() {
		ResponseType response = createRequesterErrorResponse(
				"urn:oasis:names:tc:SAML:2.0:status:RequestDenied", null);
		return response;
	}

	/**
	 * @param secondLevelStatusCode
	 *            the optional second-level status code.
	 * @param statusMessage
	 *            the optional status message.
	 * @return
	 */
	private ResponseType createRequesterErrorResponse(
			String secondLevelStatusCode, String statusMessage) {
		ResponseType response = createGenericResponse(SamlpTopLevelErrorCode.REQUESTER);

		if (null != secondLevelStatusCode) {
			StatusCodeType jaxbSecondLevelStatusCode = new StatusCodeType();
			jaxbSecondLevelStatusCode.setValue(secondLevelStatusCode);
			response.getStatus().getStatusCode().setStatusCode(
					jaxbSecondLevelStatusCode);
		}

		if (null != statusMessage) {
			response.getStatus().setStatusMessage(statusMessage);
		}
		return response;
	}

	private AssertionType getAttributeAssertion(String subjectLogin,
			Map<String, Object> attributes) {
		AssertionType assertion = new AssertionType();

		SubjectType subject = new SubjectType();
		NameIDType subjectName = new NameIDType();
		subjectName.setValue(subjectLogin);
		ObjectFactory samlObjectFactory = new ObjectFactory();
		subject.getContent().add(samlObjectFactory.createNameID(subjectName));
		assertion.setSubject(subject);

		assertion.setVersion("2.0");

		String assertionId = "urn:uuid:" + UUID.randomUUID().toString();
		assertion.setID(assertionId);

		XMLGregorianCalendar currentXmlGregorianCalendar = getCurrentXmlGregorianCalendar();
		assertion.setIssueInstant(currentXmlGregorianCalendar);

		NameIDType issuerName = new NameIDType();
		String samlAuthorityIssuerName = this.samlAuthorityService
				.getIssuerName();
		issuerName.setValue(samlAuthorityIssuerName);
		assertion.setIssuer(issuerName);

		List<StatementAbstractType> statements = assertion
				.getStatementOrAuthnStatementOrAuthzDecisionStatement();
		AttributeStatementType attributeStatement = new AttributeStatementType();
		statements.add(attributeStatement);
		List<Object> statementAttributes = attributeStatement
				.getAttributeOrEncryptedAttribute();
		for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
			String attributeName = attribute.getKey();
			Object attributeValue = attribute.getValue();

			AttributeType statementAttribute = new AttributeType();
			statementAttribute.setName(attributeName);
			List<Object> attributeValues = statementAttribute
					.getAttributeValue();
			/*
			 * attributeValue can be null.
			 */
			if (null != attributeValue && attributeValue.getClass().isArray()) {

				/*
				 * Via the "multivalued" XML attribute we communicate the type
				 * to the client.
				 */
				Map<QName, String> otherAttributes = statementAttribute
						.getOtherAttributes();
				otherAttributes.put(WebServiceConstants.MULTIVALUED_ATTRIBUTE,
						Boolean.TRUE.toString());

				/*
				 * Multivalued attribute.
				 */
				Object[] array = (Object[]) attributeValue;
				for (Object item : array) {
					attributeValues.add(item);
				}
			} else {
				attributeValues.add(attributeValue);
			}
			statementAttributes.add(statementAttribute);
		}
		return assertion;
	}

	private XMLGregorianCalendar getCurrentXmlGregorianCalendar() {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		Date now = new Date();
		gregorianCalendar.setTime(now);
		XMLGregorianCalendar currentXmlGregorianCalendar = this.datatypeFactory
				.newXMLGregorianCalendar(gregorianCalendar);
		return currentXmlGregorianCalendar;
	}
}
