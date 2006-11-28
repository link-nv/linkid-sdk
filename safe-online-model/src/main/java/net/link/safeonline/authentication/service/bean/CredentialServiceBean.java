package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.EntityNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.entity.EntityEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class CredentialServiceBean implements CredentialService {

	private static Log LOG = LogFactory.getLog(CredentialServiceBean.class);

	@EJB
	private EntityDAO entityDAO;

	public void changePassword(String login, String oldPassword,
			String newPassword) throws EntityNotFoundException,
			PermissionDeniedException {
		LOG.debug("change password of: " + login);
		EntityEntity entity = this.entityDAO.getEntity(login);
		if (!entity.getPassword().equals(oldPassword)) {
			throw new PermissionDeniedException();
		}
		entity.setPassword(newPassword);
	}
}
