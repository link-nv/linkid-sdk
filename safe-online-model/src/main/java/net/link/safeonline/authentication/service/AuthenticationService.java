package net.link.safeonline.authentication.service;

import javax.ejb.Local;

/**
 * Authentication service interface.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface AuthenticationService {
	/**
	 * Authenticates a user for a certain application. This method is used by
	 * the authentication web service.
	 * 
	 * @param applicationName
	 * @param login
	 * @param password
	 * @return
	 */
	boolean authenticate(String applicationName, String login, String password);

	/**
	 * Authenticate a user without any application subscription check. This
	 * method is used by the SafeOnline core JAAS login module.
	 * 
	 * @param login
	 * @param password
	 * @return
	 */
	boolean authenticate(String login, String password);
}
