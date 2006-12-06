package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;

/**
 * User registration service interface.
 * 
 * The component implementing this interface will allow for registration of new
 * users within the SafeOnline core. This means creating a new Subject and
 * subscribing the new Subject to the safe-online-user application.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface UserRegistrationService {

	void registerUser(String login, String password, String name)
			throws ExistingUserException, ApplicationNotFoundException;
}
