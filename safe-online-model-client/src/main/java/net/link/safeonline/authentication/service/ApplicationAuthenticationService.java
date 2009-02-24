/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;


/**
 * Interface for application authentication service.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ApplicationAuthenticationService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "ApplicationAuthenticationServiceBean/local";


    /**
     * Authenticates an application given an application certificate. At this point the application certificate already passed the PKI
     * validation.
     * 
     * @param certificate
     *            the trusted X509 application certificate.
     * @return the application Id of the authentication application.
     * @throws ApplicationNotFoundException
     */
    long authenticate(X509Certificate certificate)
            throws ApplicationNotFoundException;

    /**
     * Gives back the application X509 certificates given the application Id.
     * 
     * @param applicationId
     *            the application Id.
     * @return the X509 application certificates.
     * @throws ApplicationNotFoundException
     */
    List<X509Certificate> getCertificates(long applicationId)
            throws ApplicationNotFoundException;

    /**
     * Gives back the application X509 certificates given the application name.
     * 
     * @param applicationName
     *            the application name.
     * @return the X509 application certificates.
     * @throws ApplicationNotFoundException
     */
    List<X509Certificate> getCertificates(String applicationName)
            throws ApplicationNotFoundException;

    /**
     * Checks whether we have to skip the message integrity check. This means that the SOAP body should not be signed by the WS-Security
     * signature.
     * 
     * @param applicationId
     *            the application Id.
     * @return <code>true</code> if we can skip the message integrity check.
     * @throws ApplicationNotFoundException
     */
    boolean skipMessageIntegrityCheck(long applicationId)
            throws ApplicationNotFoundException;
}
