/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.model;

import java.security.cert.X509Certificate;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;


/**
 * Interface for PKI providers. An example of a PKI provider could be the BeID PKIX provider.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface PkiProvider extends SafeOnlineService {

    public static final String JNDI_CONTEXT = "SafeOnline/pkix";

    public static final String JNDI_PREFIX  = JNDI_CONTEXT + "/";


    /**
     * Determines whether this PKI provider COULD process the given certificate. This method should not perform a complete certificate
     * validation, that's up to the PKI validator component.
     * 
     * @param certificate
     */
    boolean accept(X509Certificate certificate);

    /**
     * Gives back the trust domain that this PKI provider requires for certificate validation.
     * 
     */
    TrustDomainEntity getTrustDomain()
            throws TrustDomainNotFoundException;

    /**
     * Gives back a reference to this EJB session object.
     * 
     */
    PkiProvider getReference();

    /**
     * Store the device attribute related to this PKI device.
     */
    void storeDeviceAttributes(SubjectEntity subject, String surname, String givenName, X509Certificate certificate);

    /**
     * Gives back the identifier domain name.
     * 
     */
    String getIdentifierDomainName();

    /**
     * Gives back the subject identifier. This identifier should be unique within the identifier domain.
     * 
     * @param certificate
     */
    String parseIdentifierFromCert(X509Certificate certificate);

    /**
     * Enables the device registration related to this PKI device.
     * 
     * @param subject
     * @param certificate
     * 
     * @throws PermissionDeniedException
     * @throws DeviceRegistrationNotFoundException
     */
    void enable(SubjectEntity subject, X509Certificate certificate)
            throws PermissionDeniedException, DeviceRegistrationNotFoundException;

    /**
     * Checks whether this device registration is disabled or not.
     * 
     * @param subject
     * @param certificate
     */
    boolean isDisabled(SubjectEntity subject, X509Certificate certificate)
            throws DeviceRegistrationNotFoundException;

    /**
     * Disables the device registration associated with the specified user attribute.
     * 
     * @param userId
     * @param attribute
     * 
     * @throws SubjectNotFoundException
     * @throws DeviceRegistrationNotFoundException
     */
    void disable(String userId, String attribute)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;

    /**
     * Removes the device registration associated with the specified user attribute.
     * 
     * @param userId
     * @param attribute
     * 
     * @throws SubjectNotFoundException
     * @throws DeviceRegistrationNotFoundException
     */
    void remove(String userId, String attribute)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;
}
