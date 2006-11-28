package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ExistingUserException;

/**
 * User registration service interface.
 * 
 * The component implementing this interface will allow for registration of new
 * users within the SafeOnline core. This means creating a new Entity and
 * subscribing the new Entity to the safe-online-user application.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface UserRegistrationService {

	static final String SAFE_ONLINE_USER_APPLICATION_NAME = "safe-online-user";

	void registerUser(String login, String password, String name)
			throws ExistingUserException;
}
