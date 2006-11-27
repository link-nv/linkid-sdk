package net.link.safeonline.authentication.service;

import javax.ejb.Local;

@Local
public interface AuthenticationService {
	boolean authenticate(String applicationName, String login, String password);
}
