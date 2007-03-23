/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.CertificateEncodingException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeTypeEntity;

/**
 * Interface to service for retrieving information about applications.
 * 
 * @author fcorneli
 * 
 */
@Local
@Remote
public interface ApplicationService {

	/**
	 * Gives back all available applications.
	 * 
	 * @return
	 */
	List<ApplicationEntity> getApplications();

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
	 * @param applicationOwnerName
	 * @param description
	 * @param encodedCertificate
	 *            the optional application certificate.
	 * @param initialApplicationIdentityAttributeTypes
	 *            the attribute types that make up the initial application
	 *            identity.
	 * @throws ExistingApplicationException
	 * @throws ApplicationOwnerNotFoundException
	 * @throws CertificateEncodingException
	 * @throws AttributeTypeNotFoundException
	 */
	void addApplication(String name, String applicationOwnerName,
			String description, byte[] encodedCertificate,
			String[] initialApplicationIdentityAttributeTypes)
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
	 * @param name
	 *            the name of the application.
	 * @param login
	 *            the admin subject login.
	 * @throws SubjectNotFoundException
	 * @throws ApplicationNotFoundException
	 * @throws ExistingApplicationOwnerException
	 */
	void registerApplicationOwner(String name, String login)
			throws SubjectNotFoundException, ApplicationNotFoundException,
			ExistingApplicationOwnerException;

	/**
	 * Gives back a list of all application owners within the system.
	 * 
	 * @return
	 */
	List<ApplicationOwnerEntity> getApplicationOwners();

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
	List<AttributeTypeEntity> getCurrentApplicationIdentity(
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
	 * @param applicationIdentityAttributeTypes
	 * @throws ApplicationNotFoundException
	 * @throws ApplicationIdentityNotFoundException
	 * @throws AttributeTypeNotFoundException
	 */
	void updateApplicationIdentity(String applicationId,
			String[] applicationIdentityAttributeTypes)
			throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException,
			AttributeTypeNotFoundException;
}
