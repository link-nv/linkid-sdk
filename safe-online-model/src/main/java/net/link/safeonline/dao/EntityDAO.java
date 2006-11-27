package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.authentication.service.EntityNotFoundException;
import net.link.safeonline.entity.EntityEntity;

/**
 * Entity Data Access Object interface.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface EntityDAO {
	/**
	 * Finds the entity for a given login. Returns <code>null</code> if the
	 * entity could not be found.
	 * 
	 * @param login
	 * @return
	 */
	EntityEntity findEntity(String login);

	EntityEntity addEntity(String login, String password);

	EntityEntity addEntity(String login, String password, String name);

	/**
	 * Gives back the entity for the given login.
	 * 
	 * @param login
	 *            the login of the entity.
	 * @return the entity.
	 * @exception EntityNotFoundException
	 */
	EntityEntity getEntity(String login) throws EntityNotFoundException;
}
