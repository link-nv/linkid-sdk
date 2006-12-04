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
	 * @return <code>true</code> if the user was authenticated correctly,
	 *         <code>false</code> otherwise.
	 */
	boolean authenticate(String applicationName, String login, String password);

	/**
	 * Authenticate a user without any application subscription check. This
	 * method is used by the SafeOnline core JAAS login module.
	 * 
	 * @param login
	 * @param password
	 * @return <code>true</code> if the user was authenticated correctly,
	 *         <code>false</code> otherwise.
	 */
	boolean authenticate(String login, String password);
}
