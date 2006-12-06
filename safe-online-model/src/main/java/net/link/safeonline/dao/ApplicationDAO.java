package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;

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
	 * @return
	 * @throws ApplicationNotFoundException
	 *             in case the application was not found.
	 */
	ApplicationEntity getApplication(String applicationName)
			throws ApplicationNotFoundException;

	void addApplication(String applicationName);

	void addApplication(String applicationName, boolean allowUserSubscription);

	void addApplication(ApplicationEntity application);

	/**
	 * Gives back a list of all application registered within the SafeOnline
	 * system.
	 * 
	 * @return
	 */
	List<ApplicationEntity> getApplications();

	void removeApplication(ApplicationEntity application);
}
