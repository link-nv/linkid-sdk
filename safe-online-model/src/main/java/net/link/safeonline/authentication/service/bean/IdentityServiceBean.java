package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.EntityNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.entity.EntityEntity;

@Stateless
public class IdentityServiceBean implements IdentityService {

	@EJB
	private EntityDAO entityDAO;

	public String getName(String login) throws EntityNotFoundException {
		/*
		 * XXX: working via login context should void the need for all the
		 * EntityNotFoundExceptions
		 */
		EntityEntity entity = this.entityDAO.getEntity(login);
		String name = entity.getName();
		return name;
	}

	public void saveName(String login, String name)
			throws EntityNotFoundException {
		/*
		 * XXX: working via login context should void the need for all the
		 * EntityNotFoundExceptions
		 */
		EntityEntity entity = this.entityDAO.getEntity(login);
		entity.setName(name);
	}
}
