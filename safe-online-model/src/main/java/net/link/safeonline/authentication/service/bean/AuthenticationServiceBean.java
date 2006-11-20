package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.dao.EntityDAO;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.entity.EntityEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class AuthenticationServiceBean implements AuthenticationService {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationServiceBean.class);

	@EJB
	private EntityDAO entityDAO;

	public boolean authenticate(String username, String password) {
		LOG.debug("authenticate: " + username);
		EntityEntity entity = this.entityDAO.findEntity(username);
		if (null == entity) {
			LOG.debug("entity not found");
			return false;
		}
		if (!entity.getPassword().equals(password)) {
			LOG.debug("password not correct");
			return false;
		}
		LOG.debug("authenticated");
		return true;
	}
}
