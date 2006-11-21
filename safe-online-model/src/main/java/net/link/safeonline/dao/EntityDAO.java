package net.link.safeonline.dao;

import javax.ejb.Local;

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
	 * Finds the entity for a given name. Returns <code>null</code> if the
	 * entity could not be found.
	 * 
	 * @param username
	 * @return
	 */
	EntityEntity findEntity(String username);

	void addEntity(String username, String password);
}
