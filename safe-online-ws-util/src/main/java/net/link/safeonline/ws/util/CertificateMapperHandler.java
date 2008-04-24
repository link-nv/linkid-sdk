/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.util;

import java.security.cert.X509Certificate;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.sdk.ws.WSSecurityServerHandler;
import net.link.safeonline.sdk.ws.WSSecurityUtil;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Certificate JAX-WS Login Handler. This JAX-WS SOAP handler maps a trusted
 * certificate to or an application Id, or a device name, or an olas node name.
 * For this it uses the {@link ApplicationAuthenticationService},
 * {@link DeviceAuthenticationService} and {@link NodeAuthenticationService}
 * service.
 * 
 * @author fcorneli
 * 
 */
public class CertificateMapperHandler implements
		SOAPHandler<SOAPMessageContext> {

	public static final String ID_PROPERTY = CertificateMapperHandler.class
			.getName()
			+ ".Id";

	private static final Log LOG = LogFactory
			.getLog(CertificateMapperHandler.class);

	private ApplicationAuthenticationService applicationAuthenticationService;

	private DeviceAuthenticationService deviceAuthenticationService;

	private NodeAuthenticationService nodeAuthenticationService;

	@PostConstruct
	public void postConstructCallback() {
		System
				.setProperty(
						"com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace",
						"true");
		this.applicationAuthenticationService = EjbUtils.getEJB(
				"SafeOnline/ApplicationAuthenticationServiceBean/local",
				ApplicationAuthenticationService.class);
		this.deviceAuthenticationService = EjbUtils.getEJB(
				"SafeOnline/DeviceAuthenticationServiceBean/local",
				DeviceAuthenticationService.class);
		this.nodeAuthenticationService = EjbUtils.getEJB(
				"SafeOnline/NodeAuthenticationServiceBean/local",
				NodeAuthenticationService.class);
	}

	public Set<QName> getHeaders() {
		return null;
	}

	@SuppressWarnings("unused")
	public void close(MessageContext context) {
		// empty
	}

	@SuppressWarnings("unused")
	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outboundProperty = (Boolean) context
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (false == outboundProperty.booleanValue()) {
			login(context);
			return true;
		}
		logout(context);
		return true;
	}

	private void login(SOAPMessageContext context) {
		LOG.debug("login");
		X509Certificate certificate = WSSecurityServerHandler
				.getCertificate(context);
		if (null == certificate) {
			throw new RuntimeException(
					"no client certificate found on JAX-WS context");
		}

		if (CertificateValidatorHandler.isDeviceCertificate(context)) {
			String deviceName;
			try {
				deviceName = this.deviceAuthenticationService
						.authenticate(certificate);
			} catch (DeviceNotFoundException e) {
				throw WSSecurityUtil.createSOAPFaultException("unknown device",
						"FailedAuthentication");
			}
			setId(deviceName, context);
			return;
		}

		if (CertificateValidatorHandler
				.isApplicationCertificate(context)) {
			String applicationId;

			try {
				applicationId = this.applicationAuthenticationService
						.authenticate(certificate);
			} catch (ApplicationNotFoundException e) {
				throw WSSecurityUtil.createSOAPFaultException(
						"unknown application", "FailedAuthentication");
			}
			setId(applicationId, context);
			return;
		}
		if (CertificateValidatorHandler.isOlasCertificate(context)) {
			String nodeName;
			try {
				nodeName = this.nodeAuthenticationService
						.authenticate(certificate);
			} catch (NodeNotFoundException e) {
				throw WSSecurityUtil.createSOAPFaultException("unknown node",
						"FailedAuthentication");
			}
			setId(nodeName, context);
			return;
		}
	}

	@SuppressWarnings("unused")
	private void logout(SOAPMessageContext context) {
		LOG.debug("logout");
	}

	private static void setId(String id, SOAPMessageContext soapMessageContext) {
		soapMessageContext.put(ID_PROPERTY, id);
	}

	/**
	 * Gives back the Id that have been written on the given SOAP message
	 * context by a handler instance of this type.
	 * 
	 * @param soapMessageContext
	 */
	public static String getId(SOAPMessageContext soapMessageContext) {
		String id = (String) soapMessageContext.get(ID_PROPERTY);
		if (null == id) {
			throw new RuntimeException("no Id found on JAX-WS context");
		}
		return id;
	}
}
