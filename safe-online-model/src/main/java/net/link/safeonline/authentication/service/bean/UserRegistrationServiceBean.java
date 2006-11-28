package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class UserRegistrationServiceBean implements UserRegistrationService {

	private static final Log LOG = LogFactory
			.getLog(UserRegistrationServiceBean.class);

	@EJB
	private EntityDAO entityDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	public void registerUser(String login, String password, String name)
			throws ExistingUserException {
		LOG.debug("register user: " + login);

		EntityEntity existingEntity = this.entityDAO.findEntity(login);
		if (null != existingEntity) {
			throw new ExistingUserException();
		}
		EntityEntity newEntity = this.entityDAO
				.addEntity(login, password, name);

		ApplicationEntity safeOnlineUserApplication = this.applicationDAO
				.findApplication(UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME);

		this.subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION,
				newEntity, safeOnlineUserApplication);
	}
}
