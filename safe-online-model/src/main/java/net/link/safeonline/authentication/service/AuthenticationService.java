package net.link.safeonline.authentication.service;

import javax.ejb.Local;

@Local
public interface AuthenticationService {
	boolean authenticate(String username, String password);
}
