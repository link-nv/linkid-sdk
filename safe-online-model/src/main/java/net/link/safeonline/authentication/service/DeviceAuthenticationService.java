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
public interface DeviceAuthenticationService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/DeviceAuthenticationServiceBean/local";


    /**
     * Authenticates a device given a device certificate. At this point the device certificate already passed the PKI validation.
     * 
     * @param certificate
     *            the trusted X509 application certificate.
     * @return the device name of the authentication device.
     * @throws DeviceNotFoundException
     */
    String authenticate(X509Certificate certificate)
            throws DeviceNotFoundException;

    TrustPointEntity findTrustPoint(String domainName, X509Certificate certificate)
            throws TrustDomainNotFoundException;

    /**
     * Gives back the device X509 certificates given the device name.
     * 
     * @param deviceName
     *            the device name.
     * @return the X509 device certificates.
     * @throws DeviceNotFoundException
     */
    List<X509Certificate> getCertificates(String deviceName)
            throws DeviceNotFoundException;

}
