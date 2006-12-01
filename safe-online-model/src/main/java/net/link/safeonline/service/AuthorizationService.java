package net.link.safeonline.service;

import java.util.Set;

import javax.ejb.Local;

/**
 * Authorization service interface. This component is used by the SafeOnline
 * core JAAS login module to assign roles to an authenticated user.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface AuthorizationService {

	Set<String> getRoles(String login);
}
