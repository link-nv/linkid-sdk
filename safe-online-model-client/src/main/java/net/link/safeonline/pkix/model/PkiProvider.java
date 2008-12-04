/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.model;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.IdentityStatementAttributes;
import net.link.safeonline.entity.AttributeEntity;
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
     * Maps from an identity statement attribute to a core attribute type.
     * 
     * @param identityStatementAttributes
     */
    String mapAttribute(IdentityStatementAttributes identityStatementAttributes);

    /**
     * Stores additional attributes. This callback method allows for PKI providers to store additional attributes related to their specific
     * device.
     * 
     * @param subject
     *            the subject for which to store additional attributes.
     * @param certificate
     */
    void storeAdditionalAttributes(SubjectEntity subject, X509Certificate certificate);

    /**
     * Store the device attribute related to this PKI device.
     * 
     * @param subject
     * @param index
     * @throws DeviceNotFoundException
     * @throws AttributeNotFoundException
     */
    void storeDeviceAttribute(SubjectEntity subject, long index)
            throws DeviceNotFoundException, AttributeNotFoundException;

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
    String getSubjectIdentifier(X509Certificate certificate);

    /**
     * Enables the device registration related to this PKI device.
     * 
     * @param subject
     * @param certificate
     * 
     * @throws DeviceNotFoundException
     * @throws AttributeTypeNotFoundException
     * @throws AttributeNotFoundException
     * @throws PermissionDeniedException
     * @throws DeviceRegistrationNotFoundException
     */
    void enable(SubjectEntity subject, X509Certificate certificate)
            throws DeviceNotFoundException, PermissionDeniedException, AttributeNotFoundException, AttributeTypeNotFoundException,
            DeviceRegistrationNotFoundException;

    /**
     * Checks whether this device registration is disabled or not.
     * 
     * @param subject
     * @param certificate
     * @throws DeviceNotFoundException
     */
    boolean isDisabled(SubjectEntity subject, X509Certificate certificate)
            throws DeviceNotFoundException;

    /**
     * Disables the device registration associated with the specified user attribute.
     * 
     * @param userId
     * @param attribute
     * 
     * @throws DeviceNotFoundException
     * @throws SubjectNotFoundException
     * @throws DeviceRegistrationNotFoundException
     */
    void disable(String userId, String attribute)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException;

    /**
     * Returns the list of device attributes attached to the specified subjects.
     * 
     * @param subject
     * @throws DeviceNotFoundException
     */
    List<AttributeEntity> listDeviceAttributes(SubjectEntity subject)
            throws DeviceNotFoundException;

    /**
     * Removes the device registration associated with the specified user attribute.
     * 
     * @param userId
     * @param attribute
     * 
     * @throws SubjectNotFoundException
     * @throws DeviceNotFoundException
     * @throws AttributeTypeNotFoundException
     * @throws DeviceRegistrationNotFoundException
     * @throws AttributeNotFoundException
     */
    void remove(String userId, String attribute)
            throws SubjectNotFoundException, DeviceNotFoundException, AttributeTypeNotFoundException, DeviceRegistrationNotFoundException,
            AttributeNotFoundException;
}
