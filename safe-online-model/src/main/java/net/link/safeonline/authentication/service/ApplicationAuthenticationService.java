/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.security.cert.X509Certificate;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;

/**
 * Interface for application authentication service.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ApplicationAuthenticationService {

	/**
	 * Authenticates an application given an application certificate. At this
	 * point the application certificate already passed the PKI validation.
	 * 
	 * @param certificate
	 *            the trusted X509 application certificate.
	 * @return the application name of the authentication application.
	 * @throws ApplicationNotFoundException
	 */
	String authenticate(X509Certificate certificate)
			throws ApplicationNotFoundException;

	/**
	 * Gives back the application X509 certificate given the application Id.
	 * 
	 * @param applicationId
	 *            the application Id.
	 * @return the X509 application certificate.
	 * @throws ApplicationNotFoundException
	 */
	X509Certificate getCertificate(String applicationId)
			throws ApplicationNotFoundException;
}
