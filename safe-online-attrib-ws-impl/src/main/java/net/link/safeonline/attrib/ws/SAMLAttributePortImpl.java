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
import javax.ejb.EJBException;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.util.ee.EjbUtils;
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

@WebService(endpointInterface = "oasis.names.tc.saml._2_0.protocol.SAMLAttributePort")
@HandlerChain(file = "saml-attrib-ws-handlers.xml")
public class SAMLAttributePortImpl implements SAMLAttributePort {

	private static final Log LOG = LogFactory
			.getLog(SAMLAttributePortImpl.class);

	private AttributeService attributeService;

	private DatatypeFactory datatypeFactory;

	@PostConstruct
	public void postConstructCallback() {
		this.attributeService = getAttributeService();

		try {
			this.datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new EJBException("datatype config error");
		}

		LOG.debug("ready");
	}

	private AttributeService getAttributeService() {
		AttributeService attributeService = EjbUtils
				.getEJB("SafeOnline/AttributeServiceBean/local",
						AttributeService.class);
		return attributeService;
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
			// TODO: return an error status
			return null;
		}
		LOG.debug("subject login: " + subjectLogin);

		List<AttributeType> attributes = request.getAttribute();
		if (null == attributes) {
			LOG.debug("no attributes");
			// TODO: return all allowed attributes
			return null;
		}
		Map<String, String> attributeMap = new HashMap<String, String>();
		for (AttributeType attribute : attributes) {
			String attributeName = attribute.getName();
			try {
				String attributeValue = this.attributeService.getAttribute(
						subjectLogin, attributeName);
				attributeMap.put(attributeName, attributeValue);
			} catch (AttributeNotFoundException e) {
				LOG.error("attribute not found: " + attributeName
						+ " for subject " + subjectLogin);
				ResponseType attributeNotFoundResponse = createAttributeNotFoundResponse(attributeName);
				return attributeNotFoundResponse;
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

	private ResponseType createAttributeNotFoundResponse(String attributeName) {
		ResponseType response = createGenericResponse(SamlpTopLevelErrorCode.REQUESTER);
		StatusCodeType secondLevelStatusCode = new StatusCodeType();
		secondLevelStatusCode
				.setValue("urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue");
		response.getStatus().getStatusCode().setStatusCode(
				secondLevelStatusCode);
		response.getStatus().setStatusMessage(
				"Attribute not found: " + attributeName);
		return response;
	}

	private AssertionType getAttributeAssertion(String subjectLogin,
			Map<String, String> attributes) {
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
		issuerName.setValue("safe-online");
		// TODO: make issuer name configurable. need global config component for
		// this
		assertion.setIssuer(issuerName);

		List<StatementAbstractType> statements = assertion
				.getStatementOrAuthnStatementOrAuthzDecisionStatement();
		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			String attributeName = attribute.getKey();
			String attributeValue = attribute.getValue();

			AttributeStatementType attributeStatement = new AttributeStatementType();
			List<Object> statementAttributes = attributeStatement
					.getAttributeOrEncryptedAttribute();
			AttributeType statementAttribute = new AttributeType();
			statementAttribute.setName(attributeName);
			List<Object> attributeValues = statementAttribute
					.getAttributeValue();
			attributeValues.add(attributeValue);
			statementAttributes.add(statementAttribute);
			statements.add(attributeStatement);
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
