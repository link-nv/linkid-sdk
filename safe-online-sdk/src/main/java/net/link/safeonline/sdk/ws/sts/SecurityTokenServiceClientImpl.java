/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.sts;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;

import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sts.ws.SecurityTokenServiceConstants;
import net.link.safeonline.sts.ws.SecurityTokenServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ObjectFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenService;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort;
import org.oasis_open.docs.ws_sx.ws_trust._200512.StatusType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ValidateTargetType;
import org.w3c.dom.Element;

/**
 * Implementation of Security Token Service Client.
 * 
 * @author fcorneli
 */
public class SecurityTokenServiceClientImpl extends AbstractMessageAccessor
		implements SecurityTokenServiceClient {

	private final static Log LOG = LogFactory
			.getLog(SecurityTokenServiceClientImpl.class);

	private final SecurityTokenServicePort port;

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
	public SecurityTokenServiceClientImpl(String location,
			X509Certificate clientCertificate, PrivateKey clientPrivateKey) {

		SecurityTokenService service = SecurityTokenServiceFactory
				.newInstance();
		this.port = service.getSecurityTokenServicePort();
		setEndpointAddress(location);

		registerMessageLoggerHandler(this.port);
		WSSecurityClientHandler.addNewHandler(this.port, clientCertificate,
				clientPrivateKey);
	}

	private void setEndpointAddress(String location) {
		BindingProvider bindingProvider = (BindingProvider) this.port;
		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				location + "/safe-online-ws/sts");
	}

	public void validate(Element token) {
		LOG.debug("invoke");
		RequestSecurityTokenType request = new RequestSecurityTokenType();
		ObjectFactory objectFactory = new ObjectFactory();
		JAXBElement<String> requestType = objectFactory
				.createRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate");
		request.getAny().add(requestType);

		JAXBElement<String> tokenType = objectFactory
				.createTokenType(SecurityTokenServiceConstants.TOKEN_TYPE_STATUS);
		request.getAny().add(tokenType);

		ValidateTargetType validateTarget = new ValidateTargetType();
		validateTarget.setAny(token);
		request.getAny()
				.add(objectFactory.createValidateTarget(validateTarget));

		SafeOnlineTrustManager.configureSsl();

		RequestSecurityTokenResponseType response;
		try {
			response = this.port.requestSecurityToken(request);
		} catch (Exception e) {
			throw retrieveHeadersFromException(e);
		} finally {
			retrieveHeadersFromPort(this.port);
		}

		StatusType status = null;
		List<Object> results = response.getAny();
		for (Object result : results)
			if (result instanceof JAXBElement) {
				JAXBElement<?> resultElement = (JAXBElement<?>) result;
				Object value = resultElement.getValue();
				if (value instanceof StatusType)
					status = (StatusType) value;
			}
		if (null == status)
			throw new RuntimeException("no Status found in response");
		String statusCode = status.getCode();
		if (SecurityTokenServiceConstants.STATUS_VALID.equals(statusCode))
			return;
		String reason = status.getReason();
		LOG.debug("reason: " + reason);
		throw new RuntimeException("token found to be invalid: " + reason);
	}
}
