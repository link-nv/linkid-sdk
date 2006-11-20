package net.link.safeonline.authentication.dao;

import javax.ejb.Local;

import net.link.safeonline.entity.EntityEntity;

@Local
public interface EntityDAO {
	EntityEntity findEntity(String username);

	void addEntity(String username, String password);
}
