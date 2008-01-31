/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.security.cert.X509Certificate;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

/**
 * Interface for device authentication service.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface DeviceAuthenticationService {

	/**
	 * Authenticates a device given a device certificate. At this point the
	 * device certificate already passed the PKI validation.
	 * 
	 * @param certificate
	 *            the trusted X509 application certificate.
	 * @return the device name of the authentication device.
	 * @throws DeviceNotFoundException
	 */
	String authenticate(X509Certificate certificate)
			throws DeviceNotFoundException;

	TrustPointEntity findTrustPoint(String domainName,
			X509Certificate certificate) throws TrustDomainNotFoundException;

	/**
	 * Gives back the devices X509 certificate given the device name.
	 * 
	 * @param deviceName
	 *            the device name.
	 * @return the X509 device certificate.
	 * @throws DeviceNotFoundException
	 */
	X509Certificate getCertificate(String deviceName)
			throws DeviceNotFoundException;

}
