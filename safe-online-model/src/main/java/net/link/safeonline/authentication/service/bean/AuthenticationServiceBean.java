package net.link.safeonline.authentication.service.bean;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.dao.HistoryDAO;
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

	@EJB
	private HistoryDAO historyDAO;

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
			Date now = new Date();
			String event = "incorrect password for application: "
					+ applicationName;
			this.historyDAO.addHistoryEntry(now, entity, event);
			LOG.debug(event);
			return false;
		}

		ApplicationEntity application = this.applicationDAO
				.findApplication(applicationName);
		if (null == application) {
			Date now = new Date();
			String event = "application not found: " + applicationName;
			this.historyDAO.addHistoryEntry(now, entity, event);
			LOG.debug(event);
			return false;
		}

		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(entity, application);
		if (null == subscription) {
			Date now = new Date();
			String event = "subscription not found for application: "
					+ applicationName;
			this.historyDAO.addHistoryEntry(now, entity, event);
			LOG.debug(event);
			return false;
		}

		LOG.debug("authenticated \"" + login + "\" for \"" + applicationName
				+ "\"");
		Date now = new Date();
		this.historyDAO.addHistoryEntry(now, entity,
				"authenticated for application " + applicationName);
		return true;
	}

	public boolean authenticate(String login, String password) {
		LOG.debug("authenticate \"" + login + "\"");

		EntityEntity entity = this.entityDAO.findEntity(login);
		if (null == entity) {
			LOG.debug("entity not found");
			return false;
		}
		if (!entity.getPassword().equals(password)) {
			Date now = new Date();
			String event = "incorrect password";
			this.historyDAO.addHistoryEntry(now, entity, event);
			LOG.debug(event);
			return false;
		}

		return true;
	}
}
