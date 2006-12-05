package net.link.safeonline.model.bean;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

/**
 * This component will initialize the system at startup.
 * 
 * For now it creates initial users, applications and subscriptions. This to
 * allow for admins to gain access to the system and thus to bootstrap the
 * SafeOnline core.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = "SafeOnline/startup/SystemInitializationStartableBean")
public class SystemInitializationStartableBean implements Startable {

	private static final Log LOG = LogFactory
			.getLog(SystemInitializationStartableBean.class);

	private static Map<String, String> authorizedUsers;

	private static List<ApplicationEntity> registeredApplications;

	private static class Subscription {
		private final String user;

		private final String application;

		private final SubscriptionOwnerType subscriptionOwnerType;

		public Subscription(SubscriptionOwnerType subscriptionOwnerType,
				String user, String application) {
			this.subscriptionOwnerType = subscriptionOwnerType;
			this.user = user;
			this.application = application;
		}
	}

	private static List<Subscription> subscriptions;

	static {
		authorizedUsers = new HashMap<String, String>();
		authorizedUsers.put("fcorneli", "secret");
		authorizedUsers.put("dieter", "secret");
		authorizedUsers.put("mario", "secret");
		authorizedUsers.put("admin", "admin");

		registeredApplications = new LinkedList<ApplicationEntity>();
		registeredApplications.add(new ApplicationEntity("demo-application"));
		registeredApplications.add(new ApplicationEntity(
				UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME));
		registeredApplications.add(new ApplicationEntity("safe-online-oper",
				false));

		subscriptions = new LinkedList<Subscription>();
		subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"fcorneli", "demo-application"));
		subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION,
				"fcorneli",
				UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME));

		subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"dieter", "demo-application"));
		subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION,
				"dieter",
				UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME));

		subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"mario", "demo-application"));
		subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION,
				"mario",
				UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME));

		subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION,
				"admin", "safe-online-oper"));
	}

	@EJB
	private SubjectDAO entityDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	public void postStart() {
		LOG.debug("start");
		for (Map.Entry<String, String> authorizedUser : authorizedUsers
				.entrySet()) {
			String login = authorizedUser.getKey();
			SubjectEntity subject = this.entityDAO.findSubject(login);
			if (null != subject) {
				continue;
			}
			String password = authorizedUser.getValue();
			this.entityDAO.addSubject(login, password);
		}

		for (ApplicationEntity application : registeredApplications) {
			String applicationName = application.getName();
			ApplicationEntity existingApplication = this.applicationDAO
					.findApplication(applicationName);
			if (null != existingApplication) {
				continue;
			}
			this.applicationDAO.addApplication(application);
		}

		for (Subscription subscription : subscriptions) {
			String login = subscription.user;
			String applicationName = subscription.application;
			SubscriptionOwnerType subscriptionOwnerType = subscription.subscriptionOwnerType;
			SubjectEntity subject = this.entityDAO.findSubject(login);
			ApplicationEntity application = this.applicationDAO
					.findApplication(applicationName);
			SubscriptionEntity subscriptionEntity = this.subscriptionDAO
					.findSubscription(subject, application);
			if (null != subscriptionEntity) {
				continue;
			}
			this.subscriptionDAO.addSubscription(subscriptionOwnerType,
					subject, application);
		}
	}

	public void preStop() {
		LOG.debug("stop");
	}
}
