package net.link.safeonline.authentication.service.bean;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of authentication service interface. This component does not
 * live within the SafeOnline core security domain (chicken-egg problem).
 * 
 * @author fcorneli
 * 
 */
@Stateless
public class AuthenticationServiceBean implements AuthenticationService {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationServiceBean.class);

	@EJB
	private SubjectDAO entityDAO;

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

		SubjectEntity subject = this.entityDAO.findSubject(login);
		if (null == subject) {
			LOG.debug("subject not found");
			return false;
		}
		if (!subject.getPassword().equals(password)) {
			Date now = new Date();
			String event = "incorrect password for application: "
					+ applicationName;
			this.historyDAO.addHistoryEntry(now, subject, event);
			LOG.debug(event);
			LOG.debug("current password: " + subject.getPassword()
					+ "; authentication password: " + password);
			return false;
		}

		ApplicationEntity application = this.applicationDAO
				.findApplication(applicationName);
		if (null == application) {
			Date now = new Date();
			String event = "application not found: " + applicationName;
			this.historyDAO.addHistoryEntry(now, subject, event);
			LOG.debug(event);
			return false;
		}

		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(subject, application);
		if (null == subscription) {
			Date now = new Date();
			String event = "subscription not found for application: "
					+ applicationName;
			this.historyDAO.addHistoryEntry(now, subject, event);
			LOG.debug(event);
			return false;
		}

		LOG.debug("authenticated \"" + login + "\" for \"" + applicationName
				+ "\"");
		Date now = new Date();
		this.historyDAO.addHistoryEntry(now, subject,
				"authenticated for application " + applicationName);
		return true;
	}

	public boolean authenticate(String login, String password) {
		LOG.debug("authenticate \"" + login + "\"");

		SubjectEntity subject = this.entityDAO.findSubject(login);
		if (null == subject) {
			LOG.debug("subject not found");
			return false;
		}
		if (!subject.getPassword().equals(password)) {
			Date now = new Date();
			String event = "incorrect password";
			this.historyDAO.addHistoryEntry(now, subject, event);
			LOG.debug(event);
			return false;
		}

		return true;
	}
}
