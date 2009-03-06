/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationAdminException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.pkix.exception.CertificateEncodingException;


/**
 * Interface to service for retrieving information about applications.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ApplicationService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "ApplicationServiceBean/local";


    /**
     * Gives back all available applications.
     * 
     */
    List<ApplicationEntity> listApplications();

    /**
     * Gives back the application entity for a given application id.
     * 
     * @param applicationId
     * @throws ApplicationNotFoundException
     */
    ApplicationEntity getApplication(long applicationId)
            throws ApplicationNotFoundException;

    /**
     * Gives back the application entity for a given application name.
     * 
     * @param applicationId
     * @throws ApplicationNotFoundException
     */
    ApplicationEntity getApplication(String applicationName)
            throws ApplicationNotFoundException;

    /**
     * Gives back the applications owned by the caller principal.
     * 
     * @throws ApplicationOwnerNotFoundException
     * 
     */
    List<ApplicationEntity> getOwnedApplications()
            throws ApplicationOwnerNotFoundException;

    /**
     * @param name
     * @param friendlyName
     * @param applicationOwnerName
     * @param description
     * @param idMappingServiceAccess
     * @param idScope
     * @param encodedCertificate
     *            the optional application certificate.
     * @param initialApplicationIdentityAttributes
     *            the optional attribute types that make up the initial application identity. Can be <code>null</code>.
     * @param skipMessageIntegrityCheck
     * @param deviceRestriction
     * @param ssoEnabled
     *            whether or not this application allows Single Sign-On
     * @param ssoLogoutUrl
     *            single sign-on logout URL, where logout requests will be sent to
     * @return The newly added application entity.
     * @throws ExistingApplicationException
     * @throws ApplicationOwnerNotFoundException
     * @throws CertificateEncodingException
     * @throws AttributeTypeNotFoundException
     */
    ApplicationEntity addApplication(String name, String friendlyName, String applicationOwnerName, String description,
                                     boolean idMappingServiceAccess, IdScopeType idScope, URL applicationUrl, byte[] newApplicationLogo,
                                     byte[] encodedCertificate, List<IdentityAttributeTypeDO> initialApplicationIdentityAttributes,
                                     boolean skipMessageIntegrityCheck, boolean deviceRestriction, boolean ssoEnabled, URL ssoLogoutUrl)
            throws ExistingApplicationException, ApplicationOwnerNotFoundException, CertificateEncodingException,
            AttributeTypeNotFoundException;

    /**
     * Removes an application an all its subscriptions.
     * 
     * @param applicationId
     */
    void removeApplication(long applicationId)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Sets the application description.
     * 
     * @param applicationId
     *            the name of the application.
     * @param description
     * @throws ApplicationNotFoundException
     * @throws PermissionDeniedException
     */
    void setApplicationDescription(long applicationId, String description)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Registers an application owner.
     * 
     * @param ownerName
     *            the name of the application owner.
     * @param adminLogin
     *            the admin subject login.
     * @throws SubjectNotFoundException
     * @throws ExistingApplicationOwnerException
     * @throws ExistingApplicationAdminException
     */
    void registerApplicationOwner(String ownerName, String adminLogin)
            throws SubjectNotFoundException, ExistingApplicationOwnerException, ExistingApplicationAdminException;

    /**
     * Removes an application owner.
     * 
     * @param ownerName
     *            the name of the application owner.
     * @param adminLogin
     *            the admin subject login.
     * @throws PermissionDeniedException
     * @throws ApplicationOwnerNotFoundException
     * @throws SubjectNotFoundException
     * @throws SubscriptionNotFoundException
     */
    void removeApplicationOwner(String ownerName, String adminLogin)
            throws SubscriptionNotFoundException, SubjectNotFoundException, ApplicationOwnerNotFoundException, PermissionDeniedException;

    /**
     * Gives back a list of all application owners within the system.
     * 
     */
    List<ApplicationOwnerEntity> listApplicationOwners();

    /**
     * Gives back a list of attribute types that make up the current application identity for the given application.
     * 
     * @param applicationId
     *            the id of the application.
     * @throws ApplicationIdentityNotFoundException
     * @throws PermissionDeniedException
     */
    Set<ApplicationIdentityAttributeEntity> getCurrentApplicationIdentity(long applicationId)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException;

    /**
     * Updates the application identity for the given application using the given set of attribute type names. The current application
     * identity version will only be changed if the new set of attribute types is a superset of the current attribute type set that makes up
     * the application identity.
     * 
     * @param applicationId
     * @param applicationIdentityAttributes
     * @throws ApplicationNotFoundException
     * @throws ApplicationIdentityNotFoundException
     * @throws AttributeTypeNotFoundException
     */
    void updateApplicationIdentity(long applicationId, List<IdentityAttributeTypeDO> applicationIdentityAttributes)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, AttributeTypeNotFoundException;

    /**
     * Updates the application URL for the given application.
     * 
     * @param applicationId
     * @param applicationUrl
     * @throws ApplicationNotFoundException
     */
    void updateApplicationUrl(long applicationId, URL applicationUrl)
            throws ApplicationNotFoundException;

    /**
     * Updates the application Logo for the given application.
     * 
     * @param applicationId
     * @param newApplicationLogo
     * @throws ApplicationNotFoundException
     */
    void updateApplicationLogo(long applicationId, byte[] newApplicationLogo)
            throws ApplicationNotFoundException;

    /**
     * Updates the X509 certificate of the given application.
     * 
     * @param applicationId
     * @param certificateData
     * @throws CertificateEncodingException
     * @throws ApplicationNotFoundException
     */
    void updateApplicationCertificate(long applicationId, byte[] certificateData)
            throws CertificateEncodingException, ApplicationNotFoundException;

    /**
     * Sets the application description.
     * 
     * @param applicationId
     * @param deviceRestriction
     * @throws ApplicationNotFoundException
     * @throws PermissionDeniedException
     */
    void setApplicationDeviceRestriction(long applicationId, boolean deviceRestriction)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Set the application's permission to use the id mapping ws.
     * 
     * @param applicationId
     * @param access
     * @throws ApplicationNotFoundException
     */
    void setIdentifierMappingServiceAccess(long applicationId, boolean access)
            throws ApplicationNotFoundException;

    /**
     * Set the application's id generation scope
     * 
     * @param applicationId
     * @param idScope
     * @throws ApplicationNotFoundException
     */
    void setIdScope(long applicationId, IdScopeType idScope)
            throws ApplicationNotFoundException;

    /**
     * Sets the message integrity check requirement for the given application.
     * 
     * @param applicationId
     * @param skipMessageIntegrityCheck
     * @throws ApplicationNotFoundException
     */
    void setSkipMessageIntegrityCheck(long applicationId, boolean skipMessageIntegrityCheck)
            throws ApplicationNotFoundException;

    /**
     * Sets if the application is Single Sign-On enabled or not.
     * 
     * @param applicationId
     * @param ssoEnabled
     * 
     * @throws ApplicationNotFoundException
     */
    void setSsoEnabled(long applicationId, boolean ssoEnabled)
            throws ApplicationNotFoundException;

    /**
     * Updates the application URL for the given application.
     * 
     * @param applicationId
     * @param ssoLogoutUrl
     * 
     * @throws ApplicationNotFoundException
     */
    void updateSsoLogoutUrl(long applicationId, URL ssoLogoutUrl)
            throws ApplicationNotFoundException;

    /**
     * Updates the application name for the given application
     * 
     * @throws ApplicationNotFoundException
     * @throws ExistingApplicationException
     */
    void updateApplicationName(long applicationId, String applicationName)
            throws ApplicationNotFoundException, ExistingApplicationException;

    /**
     * Updates the application friendly name for the given application
     * 
     * @throws ApplicationNotFoundException
     */
    void updateApplicationFriendlyName(long applicationId, String applicationFriendlyName)
            throws ApplicationNotFoundException;

    /**
     * Updates the application owner for the given application.
     * 
     * @throws ApplicationNotFoundException
     * @throws ApplicationOwnerNotFoundException
     */
    void updateApplicationOwner(long applicationId, String applicationOwner)
            throws ApplicationNotFoundException, ApplicationOwnerNotFoundException;
}
