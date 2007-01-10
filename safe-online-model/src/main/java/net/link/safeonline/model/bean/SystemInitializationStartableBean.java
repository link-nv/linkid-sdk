/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
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

	private static Map<String, String> applicationOwnersAndLogin;

	private static class Application {
		private final String name;

		private final String description;

		private final String owner;

		private final boolean allowUserSubscription;

		private final boolean removable;

		public Application(String name, String owner, String description,
				boolean allowUserSubscription, boolean removable) {
			this.name = name;
			this.owner = owner;
			this.description = description;
			this.allowUserSubscription = allowUserSubscription;
			this.removable = removable;
		}

		public Application(String name, String owner, String description) {
			this(name, owner, description, true, true);
		}

		public Application(String name, String owner) {
			this(name, owner, null);
		}
	}

	private static List<Application> registeredApplications;

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

	private static List<AttributeTypeEntity> attributeTypes;

	static {
		attributeTypes = new LinkedList<AttributeTypeEntity>();
		attributeTypes.add(new AttributeTypeEntity(
				SafeOnlineConstants.NAME_ATTRIBUTE, "string"));
		attributeTypes.add(new AttributeTypeEntity(
				SafeOnlineConstants.PASSWORD_ATTRIBUTE, "string"));
		attributeTypes.add(new AttributeTypeEntity(
				SafeOnlineConstants.GIVENNAME_ATTRIBUTE, "string"));
		attributeTypes.add(new AttributeTypeEntity(
				SafeOnlineConstants.SURNAME_ATTRIBUTE, "string"));

		authorizedUsers = new HashMap<String, String>();
		authorizedUsers.put("fcorneli", "secret");
		authorizedUsers.put("dieter", "secret");
		authorizedUsers.put("mario", "secret");
		authorizedUsers.put("admin", "admin");
		authorizedUsers.put("owner", "secret");

		applicationOwnersAndLogin = new HashMap<String, String>();
		applicationOwnersAndLogin.put("owner", "owner");

		registeredApplications = new LinkedList<Application>();
		registeredApplications
				.add(new Application("demo-application", "owner"));
		registeredApplications.add(new Application(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, "owner",
				"The SafeOnline User Web Application."));
		registeredApplications.add(new Application(
				SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME,
				"owner", "The SafeOnline Operator Web Application.", false,
				false));
		registeredApplications.add(new Application(
				SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME,
				"owner", "The SafeOnline Application Owner Web Application.",
				false, false));

		subscriptions = new LinkedList<Subscription>();
		subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"fcorneli", "demo-application"));
		subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION,
				"fcorneli",
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

		subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"dieter", "demo-application"));
		subscriptions
				.add(new Subscription(SubscriptionOwnerType.APPLICATION,
						"dieter",
						SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

		subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"mario", "demo-application"));
		subscriptions
				.add(new Subscription(SubscriptionOwnerType.APPLICATION,
						"mario",
						SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

		subscriptions
				.add(new Subscription(SubscriptionOwnerType.APPLICATION,
						"admin",
						SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));
		subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION,
				"admin",
				SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME));

		subscriptions
				.add(new Subscription(SubscriptionOwnerType.APPLICATION,
						"owner",
						SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));
		subscriptions
				.add(new Subscription(SubscriptionOwnerType.APPLICATION,
						"owner",
						SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME));
	}

	@EJB
	private SubjectDAO subjectDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private ApplicationOwnerDAO applicationOwnerDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private AttributeDAO attributeDAO;

	public void postStart() {
		LOG.debug("start");
		initAttributeTypes();

		initSubjectsAndAttributes();

		initApplicationOwners();

		initApplications();

		initSubscriptions();
	}

	private void initAttributeTypes() {
		for (AttributeTypeEntity attributeType : attributeTypes) {
			if (null != this.attributeTypeDAO.findAttributeType(attributeType
					.getName())) {
				continue;
			}
			this.attributeTypeDAO.addAttributeType(attributeType.getName(),
					attributeType.getType());
		}
	}

	private void initSubscriptions() {
		for (Subscription subscription : subscriptions) {
			String login = subscription.user;
			String applicationName = subscription.application;
			SubscriptionOwnerType subscriptionOwnerType = subscription.subscriptionOwnerType;
			SubjectEntity subject = this.subjectDAO.findSubject(login);
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

	private void initApplicationOwners() {
		for (Map.Entry<String, String> applicationOwnerAndLogin : applicationOwnersAndLogin
				.entrySet()) {
			String name = applicationOwnerAndLogin.getKey();
			String login = applicationOwnerAndLogin.getValue();
			if (null != this.applicationOwnerDAO.findApplicationOwner(name)) {
				continue;
			}
			SubjectEntity adminSubject = this.subjectDAO.findSubject(login);
			this.applicationOwnerDAO.addApplicationOwner(name, adminSubject);
		}
	}

	private void initApplications() {
		for (Application application : registeredApplications) {
			String applicationName = application.name;
			ApplicationEntity existingApplication = this.applicationDAO
					.findApplication(applicationName);
			if (null != existingApplication) {
				continue;
			}
			ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO
					.findApplicationOwner(application.owner);
			this.applicationDAO.addApplication(applicationName,
					applicationOwner, application.allowUserSubscription,
					application.removable, application.description);
		}
	}

	private void initSubjectsAndAttributes() {
		for (Map.Entry<String, String> authorizedUser : authorizedUsers
				.entrySet()) {
			String login = authorizedUser.getKey();
			SubjectEntity subject = this.subjectDAO.findSubject(login);
			if (null != subject) {
				continue;
			}
			subject = this.subjectDAO.addSubject(login);
			AttributeEntity passwordAttribute = this.attributeDAO
					.findAttribute(SafeOnlineConstants.PASSWORD_ATTRIBUTE,
							login);
			if (null != passwordAttribute) {
				continue;
			}
			String password = authorizedUser.getValue();
			this.attributeDAO.addAttribute(
					SafeOnlineConstants.PASSWORD_ATTRIBUTE, login, password);
		}
	}

	public void preStop() {
		LOG.debug("stop");
	}
}
