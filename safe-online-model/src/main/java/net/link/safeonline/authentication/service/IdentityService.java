package net.link.safeonline.authentication.service;

import javax.ejb.Local;

@Local
public interface IdentityService {

	String getName(String login) throws EntityNotFoundException;

	void saveName(String login, String name) throws EntityNotFoundException;
}
