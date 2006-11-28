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
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

/**
 * This component will initialize the system at startup.
 * 
 * For now it creates initial users, applications and subscriptions. This to
 * allow for admins to gain access to the system.
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

	private static List<String> registeredApplications;

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

		registeredApplications = new LinkedList<String>();
		registeredApplications.add("demo-application");
		registeredApplications
				.add(UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME);

		subscriptions = new LinkedList<Subscription>();
		subscriptions.add(new Subscription(SubscriptionOwnerType.ENTITY,
				"fcorneli", "demo-application"));
		subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION,
				"fcorneli",
				UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME));
		subscriptions.add(new Subscription(SubscriptionOwnerType.ENTITY,
				"dieter", "demo-application"));
		subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION,
				"dieter",
				UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME));
		subscriptions.add(new Subscription(SubscriptionOwnerType.ENTITY,
				"mario", "demo-application"));
		subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION,
				"mario",
				UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME));
	}

	@EJB
	private EntityDAO entityDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	public void start() {
		LOG.debug("start");
		for (Map.Entry<String, String> authorizedUser : authorizedUsers
				.entrySet()) {
			String login = authorizedUser.getKey();
			EntityEntity entity = this.entityDAO.findEntity(login);
			if (null != entity) {
				continue;
			}
			String password = authorizedUser.getValue();
			this.entityDAO.addEntity(login, password);
		}

		for (String applicationName : registeredApplications) {
			ApplicationEntity application = this.applicationDAO
					.findApplication(applicationName);
			if (null != application) {
				continue;
			}
			this.applicationDAO.addApplication(applicationName);
		}

		for (Subscription subscription : subscriptions) {
			String login = subscription.user;
			String applicationName = subscription.application;
			SubscriptionOwnerType subscriptionOwnerType = subscription.subscriptionOwnerType;
			EntityEntity entity = this.entityDAO.findEntity(login);
			ApplicationEntity application = this.applicationDAO
					.findApplication(applicationName);
			SubscriptionEntity subscriptionEntity = this.subscriptionDAO
					.findSubscription(entity, application);
			if (null != subscriptionEntity) {
				continue;
			}
			this.subscriptionDAO.addSubscription(subscriptionOwnerType, entity,
					application);
		}
	}

	public void stop() {
		LOG.debug("stop");
	}
}
