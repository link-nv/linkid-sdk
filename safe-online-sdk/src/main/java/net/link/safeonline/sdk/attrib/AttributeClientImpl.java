/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.attrib;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.ws.BindingProvider;

import net.link.safeonline.attrib.ws.SAMLAttributeServiceFactory;
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

public class AttributeClientImpl implements AttributeClient {

	private static final Log LOG = LogFactory.getLog(AttributeClientImpl.class);

	private SAMLAttributePort port;

	public AttributeClientImpl(String location) {
		SAMLAttributeService attributeService = SAMLAttributeServiceFactory
				.newInstance();
		this.port = attributeService.getSAMLAttributePort();

		setEndpointAddress(location);
	}

	public String getAttributeValue(String subjectLogin, String attributeName)
			throws AttributeNotFoundException {
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

		configureSsl();

		ResponseType response = this.port.attributeQuery(request);

		StatusType status = response.getStatus();
		StatusCodeType statusCode = status.getStatusCode();
		String statusCodeValue = statusCode.getValue();
		if (false == "urn:oasis:names:tc:SAML:2.0:status:Success"
				.equals(statusCodeValue)) {
			LOG.error("status code: " + statusCodeValue);
			LOG.error("status message: " + status.getStatusMessage());
			StatusCodeType secondLevelStatusCode = statusCode.getStatusCode();
			if (null != secondLevelStatusCode) {
				if ("urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue"
						.equals(secondLevelStatusCode.getValue())) {
					throw new AttributeNotFoundException();
				}
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

	private void configureSsl() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
				LOG.debug("server cert: " + certs[0]);
				LOG.debug("authentication type: " + authType);
				// TODO: verify certificate against trust repository
			}
		} };

		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
					.getSocketFactory());
		} catch (KeyManagementException e) {
			LOG.error("key management error: " + e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			LOG.error("SSL algo not present: " + e.getMessage(), e);
		}
	}
}