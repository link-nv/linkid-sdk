package net.link.safeonline.model.bean;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionEntity;

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

	private static Map<String, String> subscriptions;

	static {
		authorizedUsers = new HashMap<String, String>();
		authorizedUsers.put("fcorneli", "secret");
		authorizedUsers.put("dieter", "secret");
		authorizedUsers.put("mario", "secret");

		registeredApplications = new LinkedList<String>();
		registeredApplications.add("demo-application");

		subscriptions = new HashMap<String, String>();
		subscriptions.put("fcorneli", "demo-application");
		subscriptions.put("dieter", "demo-application");
		subscriptions.put("mario", "demo-application");
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
			String username = authorizedUser.getKey();
			EntityEntity entity = this.entityDAO.findEntity(username);
			if (null != entity) {
				continue;
			}
			String password = authorizedUser.getValue();
			this.entityDAO.addEntity(username, password);
		}

		for (String applicationName : registeredApplications) {
			ApplicationEntity application = this.applicationDAO
					.findApplication(applicationName);
			if (null != application) {
				continue;
			}
			this.applicationDAO.addApplication(applicationName);
		}

		for (Map.Entry<String, String> subscription : subscriptions.entrySet()) {
			String username = subscription.getKey();
			String applicationName = subscription.getValue();
			EntityEntity entity = this.entityDAO.findEntity(username);
			ApplicationEntity application = this.applicationDAO
					.findApplication(applicationName);
			SubscriptionEntity subscriptionEntity = this.subscriptionDAO
					.findSubscription(entity, application);
			if (null != subscriptionEntity) {
				continue;
			}
			this.subscriptionDAO.addSubscription(entity, application);
		}
	}

	public void stop() {
		LOG.debug("stop");
	}
}
