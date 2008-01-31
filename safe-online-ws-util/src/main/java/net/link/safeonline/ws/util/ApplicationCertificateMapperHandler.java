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
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Application Certificate JAX-WS Login Handler. This JAX-WS SOAP handler maps a
 * trusted certificate to an application Id. For this it uses the
 * {@link ApplicationAuthenticationService} service.
 * 
 * @author fcorneli
 * 
 */
public class ApplicationCertificateMapperHandler implements
		SOAPHandler<SOAPMessageContext> {

	public static final String APPLICATION_ID_PROPERTY = ApplicationCertificateMapperHandler.class
			.getName()
			+ ".ApplicationId";

	public static final String DEVICE_ID_PROPERTY = ApplicationCertificateMapperHandler.class
			.getName()
			+ ".DeviceId";

	private static final Log LOG = LogFactory
			.getLog(ApplicationCertificateMapperHandler.class);

	private ApplicationAuthenticationService applicationAuthenticationService;

	private DeviceAuthenticationService deviceAuthenticationService;

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

		if (ApplicationCertificateValidatorHandler.isDeviceCertificate(context)) {
			String deviceName;
			try {
				deviceName = this.deviceAuthenticationService
						.authenticate(certificate);
			} catch (DeviceNotFoundException e) {
				throw WSSecurityUtil.createSOAPFaultException("unknown device",
						"FailedAuthentication");
			}
			setDeviceName(deviceName, context);
			return;
		}

		if (ApplicationCertificateValidatorHandler
				.isApplicationCertificate(context)) {
			String applicationId;

			try {
				applicationId = this.applicationAuthenticationService
						.authenticate(certificate);
			} catch (ApplicationNotFoundException e) {
				throw WSSecurityUtil.createSOAPFaultException(
						"unknown application", "FailedAuthentication");
			}
			setApplicationId(applicationId, context);
			return;
		}
	}

	@SuppressWarnings("unused")
	private void logout(SOAPMessageContext context) {
		LOG.debug("logout");
	}

	private static void setApplicationId(String applicationId,
			SOAPMessageContext soapMessageContext) {
		soapMessageContext.put(APPLICATION_ID_PROPERTY, applicationId);
	}

	/**
	 * Gives back the application Id that have been written on the given SOAP
	 * message context by a handler instance of this type.
	 * 
	 * @param soapMessageContext
	 */
	public static String getApplicationId(SOAPMessageContext soapMessageContext) {
		String applicationId = (String) soapMessageContext
				.get(APPLICATION_ID_PROPERTY);
		if (null == applicationId) {
			throw new RuntimeException(
					"no application Id found on JAX-WS context");
		}
		return applicationId;
	}

	private static void setDeviceName(String deviceName,
			SOAPMessageContext soapMessageContext) {
		soapMessageContext.put(DEVICE_ID_PROPERTY, deviceName);
	}

	/**
	 * Gives back the device name that have been written on the given SOAP
	 * message context by a handler instance of this type.
	 * 
	 * @param soapMessageContext
	 */
	public static String getDeviceName(SOAPMessageContext soapMessageContext) {
		String deviceName = (String) soapMessageContext.get(DEVICE_ID_PROPERTY);
		if (null == deviceName) {
			throw new RuntimeException("no device name found on JAX-WS context");
		}
		return deviceName;
	}
}
