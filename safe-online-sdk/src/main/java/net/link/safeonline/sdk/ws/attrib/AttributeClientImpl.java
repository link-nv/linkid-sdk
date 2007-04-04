/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.attrib;

import java.net.ConnectException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.ws.BindingProvider;

import net.link.safeonline.attrib.ws.SAMLAttributeServiceFactory;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.ApplicationAuthenticationUtils;
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
import oasis.names.tc.saml._2_0.protocol.SAMLAttributeService;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.client.ClientTransportException;

/**
 * Implementation of attribute client. This class is using JAX-WS, secured via
 * WS-Security and server-side SSL.
 * 
 * @author fcorneli
 * 
 */
public class AttributeClientImpl implements AttributeClient {

	private static final Log LOG = LogFactory.getLog(AttributeClientImpl.class);

	private SAMLAttributePort port;

	public AttributeClientImpl(String location,
			X509Certificate clientCertificate, PrivateKey clientPrivateKey) {
		SAMLAttributeService attributeService = SAMLAttributeServiceFactory
				.newInstance();
		this.port = attributeService.getSAMLAttributePort();

		setEndpointAddress(location);

		ApplicationAuthenticationUtils.initWsSecurity(this.port,
				clientCertificate, clientPrivateKey);
	}

	public String getAttributeValue(String subjectLogin, String attributeName)
			throws AttributeNotFoundException, RequestDeniedException,
			ConnectException {
		LOG.debug("get attribute value for subject " + subjectLogin
				+ " attribute name " + attributeName);

		ObjectFactory samlObjectFactory = new ObjectFactory();

		AttributeQueryType request = new AttributeQueryType();

		SubjectType subject = new SubjectType();
		NameIDType subjectName = new NameIDType();
		subjectName.setValue(subjectLogin);
		subject.getContent().add(samlObjectFactory.createNameID(subjectName));
		request.setSubject(subject);

		List<AttributeType> attributes = request.getAttribute();
		AttributeType attribute = new AttributeType();
		attribute.setName(attributeName);
		attributes.add(attribute);

		ApplicationAuthenticationUtils.configureSsl();

		ResponseType response;
		try {
			response = this.port.attributeQuery(request);
		} catch (ClientTransportException e) {
			throw new ConnectException(e.getMessage());
		}

		StatusType status = response.getStatus();
		StatusCodeType statusCode = status.getStatusCode();
		String statusCodeValue = statusCode.getValue();
		if (false == "urn:oasis:names:tc:SAML:2.0:status:Success"
				.equals(statusCodeValue)) {
			LOG.error("status code: " + statusCodeValue);
			LOG.error("status message: " + status.getStatusMessage());
			StatusCodeType secondLevelStatusCode = statusCode.getStatusCode();
			if (null != secondLevelStatusCode) {
				String secondLevelStatusCodeValue = secondLevelStatusCode
						.getValue();
				if ("urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue"
						.equals(secondLevelStatusCodeValue)) {
					throw new AttributeNotFoundException();
				} else if ("urn:oasis:names:tc:SAML:2.0:status:RequestDenied"
						.equals(secondLevelStatusCodeValue)) {
					throw new RequestDeniedException();
				}
				LOG.debug("second level status code: "
						+ secondLevelStatusCode.getValue());
			}
			throw new RuntimeException();
		}

		List<Object> assertions = response.getAssertionOrEncryptedAssertion();
		AssertionType assertion = (AssertionType) assertions.get(0);

		List<StatementAbstractType> statements = assertion
				.getStatementOrAuthnStatementOrAuthzDecisionStatement();

		AttributeStatementType attributeStatement = (AttributeStatementType) statements
				.get(0);
		List<Object> resultAttributes = attributeStatement
				.getAttributeOrEncryptedAttribute();
		AttributeType resultAttribute = (AttributeType) resultAttributes.get(0);
		List<Object> resultAttributeValues = resultAttribute
				.getAttributeValue();
		String resultAttributeValue = (String) resultAttributeValues.get(0);

		return resultAttributeValue;
	}

	private void setEndpointAddress(String location) {
		BindingProvider bindingProvider = (BindingProvider) this.port;

		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				"https://" + location + "/safe-online-ws/attrib");
	}
}