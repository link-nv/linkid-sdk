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

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.CertificateEncodingException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;

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
	 * @throws ExistingApplicationException
	 * @throws ApplicationOwnerNotFoundException
	 * @throws CertificateEncodingException
	 */
	void addApplication(String name, String applicationOwnerName,
			String description, byte[] encodedCertificate)
			throws ExistingApplicationException,
			ApplicationOwnerNotFoundException, CertificateEncodingException;

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
	 */
	void setApplicationDescription(String name, String description)
			throws ApplicationNotFoundException;

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
}
