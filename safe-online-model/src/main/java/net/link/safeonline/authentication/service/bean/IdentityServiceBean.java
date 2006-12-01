package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.EntityNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.HistoryEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class IdentityServiceBean implements IdentityService {

	private static final Log LOG = LogFactory.getLog(IdentityServiceBean.class);

	@EJB
	private EntityDAO entityDAO;

	@EJB
	private HistoryDAO historyDAO;

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
	public String getName(String login) throws EntityNotFoundException {
		/*
		 * XXX: working via login context should void the need for all the
		 * EntityNotFoundExceptions
		 */
		EntityEntity entity = this.entityDAO.getEntity(login);
		String name = entity.getName();
		LOG.debug("get name of " + login + ": " + name);
		return name;
	}

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
	public void saveName(String login, String name)
			throws EntityNotFoundException {
		/*
		 * XXX: working via login context should void the need for all the
		 * EntityNotFoundExceptions
		 */
		EntityEntity entity = this.entityDAO.getEntity(login);
		LOG.debug("save name " + name + " for entity with login " + login);
		entity.setName(name);
	}

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
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
