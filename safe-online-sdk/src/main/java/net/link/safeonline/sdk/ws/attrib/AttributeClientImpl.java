/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.attrib;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.BindingProvider;

import net.link.safeonline.attrib.ws.SAMLAttributeServiceFactory;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.CompoundBuilder;
import net.link.safeonline.sdk.ws.CompoundUtil;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.attrib.annotation.IdentityAttribute;
import net.link.safeonline.sdk.ws.attrib.annotation.IdentityCard;
import net.link.safeonline.ws.common.SamlpSecondLevelErrorCode;
import net.link.safeonline.ws.common.SamlpTopLevelErrorCode;
import net.link.safeonline.ws.common.WebServiceConstants;
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
public class AttributeClientImpl extends AbstractMessageAccessor implements
		AttributeClient {

	private static final Log LOG = LogFactory.getLog(AttributeClientImpl.class);

	private SAMLAttributePort port;

	/**
	 * Main constructor.
	 * 
	 * @param location
	 *            the location (host:port) of the attribute web service.
	 * @param clientCertificate
	 *            the X509 certificate to use for WS-Security signature.
	 * @param clientPrivateKey
	 *            the private key corresponding with the client certificate.
	 */
	public AttributeClientImpl(String location,
			X509Certificate clientCertificate, PrivateKey clientPrivateKey) {
		SAMLAttributeService attributeService = SAMLAttributeServiceFactory
				.newInstance();
		this.port = attributeService.getSAMLAttributePort();

		setEndpointAddress(location);

		registerMessageLoggerHandler(this.port);
		WSSecurityClientHandler.addNewHandler(this.port, clientCertificate,
				clientPrivateKey);
	}

	public <Type> Type getAttributeValue(String userId, String attributeName,
			Class<Type> valueClass) throws AttributeNotFoundException,
			RequestDeniedException, ConnectException {
		LOG.debug("get attribute value for subject " + userId
				+ " attribute name " + attributeName);

		AttributeQueryType request = getAttributeQuery(userId, attributeName);

		SafeOnlineTrustManager.configureSsl();

		ResponseType response = getResponse(request);

		checkStatus(response);

		Type result = getAttributeValue(response, valueClass);
		return result;
	}

	private <Type> Type getAttributeValue(ResponseType response,
			Class<Type> valueClass) {
		List<Object> assertions = response.getAssertionOrEncryptedAssertion();
		if (assertions.isEmpty()) {
			throw new RuntimeException("No assertions in response");
		}
		AssertionType assertion = (AssertionType) assertions.get(0);

		List<StatementAbstractType> statements = assertion
				.getStatementOrAuthnStatementOrAuthzDecisionStatement();
		if (statements.isEmpty()) {
			throw new RuntimeException("No statements in response assertion");
		}
		AttributeStatementType attributeStatement = (AttributeStatementType) statements
				.get(0);
		List<Object> attributeObjects = attributeStatement
				.getAttributeOrEncryptedAttribute();
		AttributeType attribute = (AttributeType) attributeObjects.get(0);

		if (Boolean.valueOf(attribute.getOtherAttributes().get(
				WebServiceConstants.MULTIVALUED_ATTRIBUTE))
				^ valueClass.isArray()) {
			throw new IllegalArgumentException(
					"multivalued and [] type mismatch");
		}

		List<Object> attributeValues = attribute.getAttributeValue();

		if (valueClass.isArray()) {
			/*
			 * Multi-valued attribute.
			 */
			Class<?> componentType = valueClass.getComponentType();
			Type result = valueClass.cast(Array.newInstance(componentType,
					attributeValues.size()));

			int idx = 0;
			for (Object attributeValue : attributeValues) {
				if (attributeValue instanceof AttributeType) {
					AttributeType compoundAttribute = (AttributeType) attributeValue;
					CompoundBuilder compoundBuilder = new CompoundBuilder(
							componentType);

					List<Object> memberAttributes = compoundAttribute
							.getAttributeValue();
					for (Object memberAttributeObject : memberAttributes) {
						AttributeType memberAttribute = (AttributeType) memberAttributeObject;
						String memberName = memberAttribute.getName();
						Object memberAttributeValue = memberAttribute
								.getAttributeValue().get(0);
						compoundBuilder.setCompoundProperty(memberName,
								memberAttributeValue);
					}

					Array.set(result, idx, compoundBuilder.getCompound());
				} else {
					Array.set(result, idx, attributeValue);
				}
				idx++;
			}

			return result;
		}

		/*
		 * Single-valued attribute.
		 */
		// TODO: what about single-valued compounds?
		Object value = attributeValues.get(0);
		if (null == value) {
			return null;
		}

		if (false == valueClass.isInstance(value)) {
			throw new IllegalArgumentException("expected type: "
					+ valueClass.getName() + "; actual type: "
					+ value.getClass().getName());
		}
		Type attributeValue = valueClass.cast(value);
		return attributeValue;

	}

	private ResponseType getResponse(AttributeQueryType request)
			throws ConnectException {
		ResponseType response;
		try {
			response = this.port.attributeQuery(request);
		} catch (ClientTransportException e) {
			throw new ConnectException(e.getMessage());
		}
		return response;
	}

	private void checkStatus(ResponseType response)
			throws AttributeNotFoundException, RequestDeniedException {
		StatusType status = response.getStatus();
		StatusCodeType statusCode = status.getStatusCode();
		String statusCodeValue = statusCode.getValue();
		SamlpTopLevelErrorCode samlpTopLevelErrorCode = SamlpTopLevelErrorCode
				.getSamlpTopLevelErrorCode(statusCodeValue);
		if (SamlpTopLevelErrorCode.SUCCESS != samlpTopLevelErrorCode) {
			LOG.error("status code: " + statusCodeValue);
			LOG.error("status message: " + status.getStatusMessage());
			StatusCodeType secondLevelStatusCode = statusCode.getStatusCode();
			if (null != secondLevelStatusCode) {
				String secondLevelStatusCodeValue = secondLevelStatusCode
						.getValue();
				SamlpSecondLevelErrorCode samlpSecondLevelErrorCode = SamlpSecondLevelErrorCode
						.getSamlpTopLevelErrorCode(secondLevelStatusCodeValue);
				if (SamlpSecondLevelErrorCode.INVALID_ATTRIBUTE_NAME_OR_VALUE == samlpSecondLevelErrorCode) {
					throw new AttributeNotFoundException();
				} else if (SamlpSecondLevelErrorCode.REQUEST_DENIED == samlpSecondLevelErrorCode) {
					throw new RequestDeniedException();
				}
				LOG.debug("second level status code: "
						+ secondLevelStatusCode.getValue());
			}
			throw new RuntimeException("error: " + statusCodeValue);
		}
	}

	private void setEndpointAddress(String location) {
		BindingProvider bindingProvider = (BindingProvider) this.port;

		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				"https://" + location + "/safe-online-ws/attrib");
	}

	private AttributeQueryType getAttributeQuery(String subjectLogin,
			String attributeName) {
		Set<String> attributeNames = Collections.singleton(attributeName);
		AttributeQueryType attributeQuery = getAttributeQuery(subjectLogin,
				attributeNames);
		return attributeQuery;
	}

	private AttributeQueryType getAttributeQuery(String subjectLogin,
			Set<String> attributeNames) {
		ObjectFactory samlObjectFactory = new ObjectFactory();
		AttributeQueryType attributeQuery = new AttributeQueryType();
		SubjectType subject = new SubjectType();
		NameIDType subjectName = new NameIDType();
		subjectName.setValue(subjectLogin);
		subject.getContent().add(samlObjectFactory.createNameID(subjectName));
		attributeQuery.setSubject(subject);

		List<AttributeType> attributes = attributeQuery.getAttribute();
		for (String attributeName : attributeNames) {
			AttributeType attribute = new AttributeType();
			attribute.setName(attributeName);
			attributes.add(attribute);
		}
		return attributeQuery;
	}

	private AttributeQueryType getAttributeQuery(String subjectLogin,
			Map<String, Object> attributes) {
		Set<String> attributeNames = attributes.keySet();
		AttributeQueryType attributeQuery = getAttributeQuery(subjectLogin,
				attributeNames);
		return attributeQuery;
	}

	public void getAttributeValues(String userId, Map<String, Object> attributes)
			throws AttributeNotFoundException, RequestDeniedException,
			ConnectException {
		AttributeQueryType request = getAttributeQuery(userId, attributes);
		SafeOnlineTrustManager.configureSsl();
		ResponseType response = getResponse(request);
		checkStatus(response);
		getAttributeValues(response, attributes);
	}

	private void getAttributeValues(ResponseType response,
			Map<String, Object> attributes) {
		List<Object> assertions = response.getAssertionOrEncryptedAssertion();
		if (0 == assertions.size()) {
			throw new RuntimeException("No assertions in response");
		}
		AssertionType assertion = (AssertionType) assertions.get(0);

		List<StatementAbstractType> statements = assertion
				.getStatementOrAuthnStatementOrAuthzDecisionStatement();
		if (0 == statements.size()) {
			throw new RuntimeException("No statements in response assertion");
		}
		AttributeStatementType attributeStatement = (AttributeStatementType) statements
				.get(0);
		List<Object> attributeObjects = attributeStatement
				.getAttributeOrEncryptedAttribute();
		for (Object attributeObject : attributeObjects) {
			AttributeType attribute = (AttributeType) attributeObject;
			String attributeName = attribute.getName();
			List<Object> attributeValues = attribute.getAttributeValue();

			Object attributeValue;
			if (true == Boolean.valueOf(attribute.getOtherAttributes().get(
					WebServiceConstants.MULTIVALUED_ATTRIBUTE))) {
				/*
				 * We use the first attribute value to determine the type of the
				 * array to be returned.
				 */
				Object firstAttributeValue = attributeValues.get(0);
				Class<?> componentType = firstAttributeValue.getClass();
				int size = attributeValues.size();
				attributeValue = Array.newInstance(componentType, size);
				for (int idx = 0; idx < size; idx++) {
					Array.set(attributeValue, idx, attributeValues.get(idx));
				}
			} else {
				/*
				 * Single-valued attribute.
				 * 
				 * Here we depend on the xsi:type typing.
				 */
				attributeValue = attributeValues.get(0);
			}

			attributes.put(attributeName, attributeValue);
		}
	}

	public Map<String, Object> getAttributeValues(String userId)
			throws RequestDeniedException, ConnectException,
			AttributeNotFoundException {
		Map<String, Object> attributes = new HashMap<String, Object>();
		AttributeQueryType request = getAttributeQuery(userId, attributes);
		SafeOnlineTrustManager.configureSsl();
		ResponseType response = getResponse(request);
		checkStatus(response);
		getAttributeValues(response, attributes);
		return attributes;
	}

	@SuppressWarnings("unchecked")
	public <Type> Type getIdentity(String subjectLogin,
			Class<Type> identityCardClass) throws AttributeNotFoundException,
			RequestDeniedException, ConnectException {
		IdentityCard identityCardAnnotation = identityCardClass
				.getAnnotation(IdentityCard.class);
		if (null == identityCardAnnotation) {
			throw new IllegalArgumentException(
					"identity card class should be annotated with @IdentityCard");
		}
		Type identityCard;
		try {
			identityCard = identityCardClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(
					"could not instantiate the identity card class");
		}
		Method[] methods = identityCardClass.getMethods();
		for (Method method : methods) {
			IdentityAttribute identityAttributeAnnotation = method
					.getAnnotation(IdentityAttribute.class);
			if (null == identityAttributeAnnotation) {
				continue;
			}
			String attributeName = identityAttributeAnnotation.value();
			Class valueClass = method.getReturnType();
			Object attributeValue = getAttributeValue(subjectLogin,
					attributeName, valueClass);
			Method setMethod = CompoundUtil.getSetMethod(identityCardClass,
					method);
			try {
				setMethod.invoke(identityCard, new Object[] { attributeValue });
			} catch (Exception e) {
				throw new RuntimeException("error: " + e.getMessage());
			}
		}
		return identityCard;
	}
}