package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class AuthenticationServiceBean implements AuthenticationService {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationServiceBean.class);

	@EJB
	private EntityDAO entityDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	public boolean authenticate(String applicationName, String login,
			String password) {
		LOG.debug("authenticate \"" + login + "\" for \"" + applicationName
				+ "\"");

		EntityEntity entity = this.entityDAO.findEntity(login);
		if (null == entity) {
			LOG.debug("entity not found");
			return false;
		}
		if (!entity.getPassword().equals(password)) {
			LOG.debug("password not correct");
			return false;
		}

		ApplicationEntity application = this.applicationDAO
				.findApplication(applicationName);
		if (null == application) {
			LOG.debug("application not found");
			return false;
		}

		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(entity, application);
		if (null == subscription) {
			LOG.debug("subscription not found");
			return false;
		}

		LOG.debug("authenticated \"" + login + "\" for \"" + applicationName
				+ "\"");
		return true;
	}
}
