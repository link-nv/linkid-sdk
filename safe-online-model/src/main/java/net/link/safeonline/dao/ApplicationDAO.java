package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.entity.ApplicationEntity;

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

	void addApplication(String applicationName);
}
