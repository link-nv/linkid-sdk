package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.ApplicationEntity;

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
	List<ApplicationEntity> getApplications();

	void addApplication(String name, String description);
}
