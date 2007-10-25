/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.sts;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.xml.ws.BindingProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenService;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort;

import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sts.ws.SecurityTokenServiceFactory;

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
				"https://" + location + "/safe-online-ws/sts");
	}
	
	public void invoke() {
		LOG.debug("invoke");
		RequestSecurityTokenType request = new RequestSecurityTokenType();
		
		SafeOnlineTrustManager.configureSsl();
		
		RequestSecurityTokenResponseType response = this.port.requestSecurityToken(request);
	}
}
