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

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.UsageAgreementNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.pkix.exception.CertificateEncodingException;

/**
 * Interface to service for retrieving information about applications.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ApplicationService {

	/**
	 * Gives back all available applications.
	 * 
	 * @return
	 */
	List<ApplicationEntity> listApplications();

	/**
	 * Gives back the application entity for a given application name.
	 * 
	 * @param applicationName
	 * @return
	 * @throws ApplicationNotFoundException
	 */
	ApplicationEntity getApplication(String applicationName)
			throws ApplicationNotFoundException;

	/**
	 * Gives back the applications owned by the caller principal.
	 * 
	 * @return
	 */
	List<ApplicationEntity> getOwnedApplications();

	/**
	 * @param name
	 * @param friendlyName
	 * @param applicationOwnerName
	 * @param description
	 * @param idMappingServiceAccess
	 * @param encodedCertificate
	 *            the optional application certificate.
	 * @param initialApplicationIdentityAttributes
	 *            the optional attribute types that make up the initial
	 *            application identity. Can be <code>null</code>.
	 * @throws ExistingApplicationException
	 * @throws ApplicationOwnerNotFoundException
	 * @throws CertificateEncodingException
	 * @throws AttributeTypeNotFoundException
	 */
	void addApplication(String name, String friendlyName,
			String applicationOwnerName, String description,
			boolean idMappingServiceAccess, URL applicationUrl,
			byte[] encodedCertificate,
			List<IdentityAttributeTypeDO> initialApplicationIdentityAttributes)
			throws ExistingApplicationException,
			ApplicationOwnerNotFoundException, CertificateEncodingException,
			AttributeTypeNotFoundException;

	/**
	 * Removes an application an all its subscriptions.
	 * 
	 * @param name
	 */
	void removeApplication(String name) throws ApplicationNotFoundException,
			PermissionDeniedException;

	/**
	 * Sets the application description.
	 * 
	 * @param name
	 *            the name of the application.
	 * @param description
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	void setApplicationDescription(String name, String description)
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
	 */
	void registerApplicationOwner(String ownerName, String adminId)
			throws SubjectNotFoundException, ExistingApplicationOwnerException;

	/**
	 * Gives back a list of all application owners within the system.
	 * 
	 * @return
	 */
	List<ApplicationOwnerEntity> listApplicationOwners();

	/**
	 * Gives back a list of attribute types that make up the current application
	 * identity for the given application.
	 * 
	 * @param applicationName
	 *            the name of the application.
	 * @return
	 * @throws ApplicationIdentityNotFoundException
	 * @throws PermissionDeniedException
	 */
	Set<ApplicationIdentityAttributeEntity> getCurrentApplicationIdentity(
			String applicationName) throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException, PermissionDeniedException;

	/**
	 * Updates the application identity for the given application using the
	 * given set of attribute type names. The current application identity
	 * version will only be changed if the new set of attribute types is a
	 * superset of the current attribute type set that makes up the application
	 * identity.
	 * 
	 * @param applicationId
	 * @param applicationIdentityAttributes
	 * @throws ApplicationNotFoundException
	 * @throws ApplicationIdentityNotFoundException
	 * @throws AttributeTypeNotFoundException
	 */
	void updateApplicationIdentity(String applicationId,
			List<IdentityAttributeTypeDO> applicationIdentityAttributes)
			throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException,
			AttributeTypeNotFoundException;

	/**
	 * Updates the application URL for the given application.
	 * 
	 * @param applicationId
	 * @param applicationUrl
	 * @throws ApplicationNotFoundException
	 */
	void updateApplicationUrl(String applicationId, URL applicationUrl)
			throws ApplicationNotFoundException;

	/**
	 * Updates the X509 certificate of the given application.
	 * 
	 * @param applicationName
	 * @param certificateData
	 * @throws CertificateEncodingException
	 * @throws ApplicationNotFoundException
	 */
	void updateApplicationCertificate(String applicationName,
			byte[] certificateData) throws CertificateEncodingException,
			ApplicationNotFoundException;

	/**
	 * Sets the application description.
	 * 
	 * @param name
	 *            the name of the application.
	 * @param deviceRestriction
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	void setApplicationDeviceRestriction(String applicationName,
			boolean deviceRestriction) throws ApplicationNotFoundException,
			PermissionDeniedException;

	void setIdentifierMappingServiceAccess(String applicationName,
			boolean access) throws ApplicationNotFoundException;

	/**
	 * Update the draft usage agreement text for the specified language. If no
	 * draft was present, create it ...
	 * 
	 * @param applicationName
	 * @param language
	 * @param text
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	void updateUsageAgreementText(String applicationName, String language,
			String text) throws ApplicationNotFoundException,
			PermissionDeniedException;

	/**
	 * Commits the draft usage agreement to a new version.
	 * 
	 * @param applicationName
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 * @throws UsageAgreementNotFoundException
	 */
	void updateUsageAgreement(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException,
			UsageAgreementNotFoundException;

	/**
	 * Returns currently associate usage agreement with the specified
	 * application.
	 * 
	 * @param applicationName
	 * @return
	 * @throws PermissionDeniedException
	 * @throws ApplicationNotFoundException
	 */
	UsageAgreementEntity getCurrentUsageAgreement(String applicationName)
			throws PermissionDeniedException, ApplicationNotFoundException;

	/**
	 * Get all usage agreements of the specified application.
	 * 
	 * @param applicationName
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	List<UsageAgreementEntity> getUsageAgreements(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException;
}
