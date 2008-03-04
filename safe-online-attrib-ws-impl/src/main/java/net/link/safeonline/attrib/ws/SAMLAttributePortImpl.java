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
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.authentication.service.NodeAttributeService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.model.ApplicationManager;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.ws.common.SamlpSecondLevelErrorCode;
import net.link.safeonline.ws.common.SamlpTopLevelErrorCode;
import net.link.safeonline.ws.common.WebServiceConstants;
import net.link.safeonline.ws.util.ApplicationCertificateValidatorHandler;
import net.link.safeonline.ws.util.CertificateDomainException;
import net.link.safeonline.ws.util.ApplicationCertificateValidatorHandler.CertificateDomain;
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
 * 
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

	@EJB(mappedName = "SafeOnline/NodeAttributeServiceBean/local")
	private NodeAttributeService nodeAttributeService;

	@EJB(mappedName = "SafeOnline/SamlAuthorityServiceBean/local")
	private SamlAuthorityService samlAuthorityService;

	@Resource
	private WebServiceContext context;

	private DatatypeFactory datatypeFactory;

	private CertificateDomain certificateDomain;

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
		for (JAXBElement<?> subjectItem : subjectContent) {
			Object value = subjectItem.getValue();
			if (false == value instanceof NameIDType) {
				continue;
			}
			NameIDType nameId = (NameIDType) value;
			String subjectLogin = nameId.getValue();
			if (this.certificateDomain.equals(CertificateDomain.APPLICATION)) {
				try {
					return getUserId(subjectLogin);
				} catch (ApplicationNotFoundException e) {
					return null;
				}
			}
			return subjectLogin;
		}
		return null;
	}

	private String getUserId(String applicationUserId)
			throws ApplicationNotFoundException {
		ApplicationManager applicationManager = EjbUtils.getEJB(
				"SafeOnline/ApplicationManagerBean/local",
				ApplicationManager.class);
		ApplicationEntity application = applicationManager
				.getCallerApplication();

		UserIdMappingService userIdMappingService = EjbUtils.getEJB(
				"SafeOnline/UserIdMappingServiceBean/local",
				UserIdMappingService.class);
		return userIdMappingService.getUserId(application.getName(),
				applicationUserId);
	}

	public ResponseType attributeQuery(AttributeQueryType request) {
		LOG.debug("attribute query");

		try {
			this.certificateDomain = ApplicationCertificateValidatorHandler
					.getCertificateDomain(this.context);
		} catch (CertificateDomainException e) {
			ResponseType requestDeniedResponse = createRequestDeniedResponse();
			return requestDeniedResponse;
		}
		LOG.debug("certificate domain: " + this.certificateDomain.toString());

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
				attributeMap = getAttributeValues(subjectLogin);
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
					Object attributeValue = getAttributeValue(subjectLogin,
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
				} catch (AttributeTypeNotFoundException e) {
					LOG.error("attribute not found: " + attributeName
							+ " for subject " + subjectLogin);
					ResponseType attributeNotFoundResponse = createAttributeNotFoundResponse(attributeName);
					return attributeNotFoundResponse;
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

	private Object getAttributeValue(String subjectLogin, String attributeName)
			throws SubjectNotFoundException, AttributeNotFoundException,
			PermissionDeniedException, AttributeTypeNotFoundException {
		if (this.certificateDomain.equals(CertificateDomain.APPLICATION))
			return this.attributeService.getConfirmedAttributeValue(
					subjectLogin, attributeName);
		if (this.certificateDomain.equals(CertificateDomain.OLAS))
			return this.nodeAttributeService.getAttributeValue(subjectLogin,
					attributeName);
		return null;
	}

	private Map<String, Object> getAttributeValues(String subjectLogin)
			throws SubjectNotFoundException, PermissionDeniedException {
		if (this.certificateDomain.equals(CertificateDomain.APPLICATION))
			return this.attributeService
					.getConfirmedAttributeValues(subjectLogin);
		return null;
	}

	private ResponseType createGenericResponse(SamlpTopLevelErrorCode errorCode) {
		ResponseType response = new ResponseType();
		String responseId = "urn:uuid:" + UUID.randomUUID().toString();
		response.setID(responseId);
		response.setVersion("2.0");
		XMLGregorianCalendar currentXmlGregorianCalendar = getCurrentXmlGregorianCalendar();
		response.setIssueInstant(currentXmlGregorianCalendar);
		StatusCodeType statusCode = new StatusCodeType();
		statusCode.setValue(errorCode.getErrorCode());
		StatusType status = new StatusType();
		status.setStatusCode(statusCode);
		response.setStatus(status);
		return response;
	}

	/**
	 * @param attributeName
	 *            the optional attribute name.
	 */
	private ResponseType createAttributeNotFoundResponse(String attributeName) {
		String detailMessage;
		if (null == attributeName) {
			detailMessage = "Attribute not found.";
		} else {
			detailMessage = "Attribute not found: " + attributeName;
		}
		ResponseType response = createRequesterErrorResponse(
				SamlpSecondLevelErrorCode.INVALID_ATTRIBUTE_NAME_OR_VALUE,
				detailMessage);
		return response;
	}

	private ResponseType createUnknownPrincipalResponse(String subjectLogin) {
		ResponseType response = createRequesterErrorResponse(
				SamlpSecondLevelErrorCode.UNKNOWN_PRINCIPAL,
				"Subject not found: " + subjectLogin);
		return response;
	}

	private ResponseType createRequestDeniedResponse() {
		ResponseType response = createRequesterErrorResponse(
				SamlpSecondLevelErrorCode.REQUEST_DENIED, null);
		return response;
	}

	/**
	 * @param secondLevelStatusCode
	 *            the optional second-level status code.
	 * @param statusMessage
	 *            the optional status message.
	 */
	private ResponseType createRequesterErrorResponse(
			SamlpSecondLevelErrorCode secondLevelStatusCode,
			String statusMessage) {
		ResponseType response = createGenericResponse(SamlpTopLevelErrorCode.REQUESTER);

		if (null != secondLevelStatusCode) {
			StatusCodeType jaxbSecondLevelStatusCode = new StatusCodeType();
			jaxbSecondLevelStatusCode.setValue(secondLevelStatusCode
					.getErrorCode());
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
			statementAttribute
					.setNameFormat(WebServiceConstants.SAML_ATTRIB_NAME_FORMAT_BASIC);
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
					if (item instanceof Map) {
						/*
						 * Compounded attribute.
						 */
						@SuppressWarnings("unchecked")
						Map<String, Object> compoundedAttributeValues = (Map<String, Object>) item;

						AttributeType compoundedAttribute = new AttributeType();

						compoundedAttribute
								.setNameFormat(WebServiceConstants.SAML_ATTRIB_NAME_FORMAT_BASIC);
						compoundedAttribute.setName(attributeName);
						for (Map.Entry<String, Object> compoundedAttributeValue : compoundedAttributeValues
								.entrySet()) {
							AttributeType memberAttribute = new AttributeType();
							memberAttribute
									.setNameFormat(WebServiceConstants.SAML_ATTRIB_NAME_FORMAT_BASIC);
							memberAttribute.setName(compoundedAttributeValue
									.getKey());
							memberAttribute.getAttributeValue().add(
									compoundedAttributeValue.getValue());
							compoundedAttribute.getAttributeValue().add(
									memberAttribute);
						}
						attributeValues.add(compoundedAttribute);

					} else {
						attributeValues.add(item);
					}
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
