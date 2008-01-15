/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.awt.Color;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;

/**
 * Application entity data access object interface definition.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ApplicationDAO {
	/**
	 * Find the application for a given application name.
	 * 
	 * @param applicationName
	 *            the application name.
	 * @return the application or <code>null</code> if not found.
	 */
	ApplicationEntity findApplication(String applicationName);

	/**
	 * Gives back the application entity for a given application name.
	 * 
	 * @param applicationName
	 * @throws ApplicationNotFoundException
	 *             in case the application was not found.
	 */
	ApplicationEntity getApplication(String applicationName)
			throws ApplicationNotFoundException;

	ApplicationEntity addApplication(String applicationName,
			String applicationFriendlyName,
			ApplicationOwnerEntity applicationOwner, String description,
			URL applicationUrl, byte[] applicationLogo, Color applicationColor,
			X509Certificate certificate);

	ApplicationEntity addApplication(String applicationName,
			String applicationFriendlyName,
			ApplicationOwnerEntity applicationOwner,
			boolean allowUserSubscription, boolean removable,
			String description, URL applicationUrl, byte[] applicationLogo,
			Color applicationColor, X509Certificate certificate,
			long initialIdentityVersion, long usageAgreementVersion);

	/**
	 * Gives back a list of all application registered within the SafeOnline
	 * system.
	 * 
	 */
	List<ApplicationEntity> listApplications();

	/**
	 * Gives back a list of all applications registered within the SafeOnline
	 * system and allowed for regular users to view/subscribe to.
	 * 
	 */
	List<ApplicationEntity> listUserApplications();

	/**
	 * Gives back the application owned by the given application owner.
	 * 
	 * @param applicationOwner
	 */
	List<ApplicationEntity> listApplications(
			ApplicationOwnerEntity applicationOwner);

	void removeApplication(ApplicationEntity application);

	/**
	 * Gives back an application entity.
	 * 
	 * @param certificate
	 *            the application certificate.
	 * @throws ApplicationNotFoundException
	 */
	ApplicationEntity getApplication(X509Certificate certificate)
			throws ApplicationNotFoundException;
}
