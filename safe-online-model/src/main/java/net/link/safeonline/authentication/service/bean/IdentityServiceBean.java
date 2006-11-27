package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.EntityNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.HistoryEntity;

@Stateless
public class IdentityServiceBean implements IdentityService {

	@EJB
	private EntityDAO entityDAO;

	@EJB
	private HistoryDAO historyDAO;

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

	public List<HistoryEntity> getHistory(String login)
			throws EntityNotFoundException {
		/*
		 * XXX: working via login context should void the need for all the
		 * EntityNotFoundExceptions
		 */
		EntityEntity entity = this.entityDAO.getEntity(login);
		List<HistoryEntity> result = this.historyDAO.getHistory(entity);
		return result;
	}
}
