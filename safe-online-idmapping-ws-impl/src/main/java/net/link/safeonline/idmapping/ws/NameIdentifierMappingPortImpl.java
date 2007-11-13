/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.idmapping.ws;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentifierMappingService;
import net.link.safeonline.ws.common.SamlpSecondLevelErrorCode;
import net.link.safeonline.ws.common.SamlpTopLevelErrorCode;
import net.link.safeonline.ws.util.ri.Injection;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.NameIDMappingRequestType;
import oasis.names.tc.saml._2_0.protocol.NameIDMappingResponseType;
import oasis.names.tc.saml._2_0.protocol.NameIDPolicyType;
import oasis.names.tc.saml._2_0.protocol.NameIdentifierMappingPort;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of SAML Name Identifier Mapping Service.
 * 
 * <p>
 * Specification: Assertions and Protocols for the OASIS Security Assertion
 * Markup Language (SAML) V2.0.
 * </p>
 * 
 * @author fcorneli
 * 
 */
@WebService(endpointInterface = "oasis.names.tc.saml._2_0.protocol.NameIdentifierMappingPort")
@HandlerChain(file = "app-auth-ws-handlers.xml")
@Injection
public class NameIdentifierMappingPortImpl implements NameIdentifierMappingPort {

	private static final Log LOG = LogFactory
			.getLog(NameIdentifierMappingPortImpl.class);

	@EJB(mappedName = "SafeOnline/IdentifierMappingServiceBean/local")
	private IdentifierMappingService identifierMappingService;

	public NameIDMappingResponseType nameIdentifierMappingQuery(
			NameIDMappingRequestType request) {
		LOG.debug("name identifier mapping query");

		NameIDPolicyType nameIdPolicy = request.getNameIDPolicy();
		String nameIdFormat = nameIdPolicy.getFormat();
		if (null == nameIdFormat
				|| false == NameIdentifierMappingConstants.NAMEID_FORMAT_PERSISTENT
						.equals(nameIdFormat)) {
			NameIDMappingResponseType response = createErrorResponse(SamlpSecondLevelErrorCode.INVALID_NAMEID_POLICY);
			return response;
		}

		NameIDType nameId = request.getNameID();
		if (null == nameId) {
			LOG.debug("missing NameID element");
			NameIDMappingResponseType response = createErrorResponse(null);
			return response;
		}

		String username = nameId.getValue();
		if (null == username) {
			LOG.debug("missing NameID value");
			NameIDMappingResponseType response = createErrorResponse(null);
			return response;
		}
		LOG.debug("username: " + username);

		String userId;
		try {
			userId = this.identifierMappingService.getUserId(username);
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied: " + e.getMessage());
			NameIDMappingResponseType response = createErrorResponse(SamlpSecondLevelErrorCode.REQUEST_DENIED);
			return response;
		} catch (SubscriptionNotFoundException e) {
			LOG.debug("subscription not found: " + username);
			NameIDMappingResponseType response = createErrorResponse(SamlpSecondLevelErrorCode.REQUEST_DENIED);
			return response;
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found: " + username);
			NameIDMappingResponseType response = createErrorResponse(SamlpSecondLevelErrorCode.REQUEST_DENIED);
			return response;
		} catch (SubjectNotFoundException e) {
			LOG.debug("subject not found: " + username);
			NameIDMappingResponseType response = createErrorResponse(SamlpSecondLevelErrorCode.REQUEST_DENIED);
			return response;
		}
		LOG.debug("userId: " + userId);

		NameIDMappingResponseType response = createGenericResponse(SamlpTopLevelErrorCode.SUCCESS);
		NameIDType responseNameId = new NameIDType();
		responseNameId.setValue(userId);
		response.setNameID(responseNameId);

		return response;
	}

	private NameIDMappingResponseType createGenericResponse(
			SamlpTopLevelErrorCode topLevelErrorCode) {
		NameIDMappingResponseType response = new NameIDMappingResponseType();
		String responseId = "urn:uuid:" + UUID.randomUUID().toString();
		response.setID(responseId);
		response.setVersion("2.0");
		XMLGregorianCalendar currentXmlGregorianCalendar = getCurrentXmlGregorianCalendar();
		response.setIssueInstant(currentXmlGregorianCalendar);
		StatusCodeType statusCode = new StatusCodeType();
		statusCode.setValue(topLevelErrorCode.getErrorCode());
		StatusType status = new StatusType();
		status.setStatusCode(statusCode);
		response.setStatus(status);
		return response;
	}

	private NameIDMappingResponseType createErrorResponse(
			SamlpSecondLevelErrorCode secondLevelErrorCode) {
		NameIDMappingResponseType response = createGenericResponse(SamlpTopLevelErrorCode.RESPONDER);
		if (null != secondLevelErrorCode) {
			StatusCodeType secondLevelStatusCode = new StatusCodeType();
			secondLevelStatusCode.setValue(secondLevelErrorCode.getErrorCode());
			response.getStatus().getStatusCode().setStatusCode(
					secondLevelStatusCode);
		}
		return response;
	}

	private XMLGregorianCalendar getCurrentXmlGregorianCalendar() {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		Date now = new Date();
		gregorianCalendar.setTime(now);
		XMLGregorianCalendar currentXmlGregorianCalendar = this.datatypeFactory
				.newXMLGregorianCalendar(gregorianCalendar);
		return currentXmlGregorianCalendar;
	}

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
}
