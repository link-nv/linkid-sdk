/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

/**
 * Implementation of application service interface.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class ApplicationServiceBean implements ApplicationService {

	private static final Log LOG = LogFactory
			.getLog(ApplicationServiceBean.class);

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private SubjectDAO subjectDAO;

	@PermitAll
	public List<ApplicationEntity> getApplications() {
		List<ApplicationEntity> applications = this.applicationDAO
				.getApplications();
		return applications;
	}

	@RolesAllowed(SafeOnlineConstants.OPERATOR_ROLE)
	public void addApplication(String name, String description)
			throws ExistingApplicationException {
		LOG.debug("add application: " + name);
		ApplicationEntity existingApplication = this.applicationDAO
				.findApplication(name);
		if (null != existingApplication) {
			throw new ExistingApplicationException();
		}
		ApplicationEntity newApplication = new ApplicationEntity(name);
		newApplication.setDescription(description);
		this.applicationDAO.addApplication(newApplication);
	}

	@RolesAllowed(SafeOnlineConstants.OPERATOR_ROLE)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeApplication(String name)
			throws ApplicationNotFoundException, PermissionDeniedException {
		LOG.debug("remove application: " + name);
		ApplicationEntity application = this.applicationDAO
				.getApplication(name);
		if (!application.isRemovable()) {
			throw new PermissionDeniedException();
		}
		List<SubscriptionEntity> subscriptions = this.subscriptionDAO
				.getSubscriptions(application);
		/*
		 * We don't rely on hibernate here to cascade remove the subscriptions
		 * for the moment.
		 */
		for (SubscriptionEntity subscription : subscriptions) {
			this.subscriptionDAO.removeSubscription(subscription);
		}
		this.applicationDAO.removeApplication(application);
	}

	@RolesAllowed(SafeOnlineConstants.OWNER_ROLE)
	public void setApplicationDescription(String name, String description)
			throws ApplicationNotFoundException {
		LOG.debug("set application description: " + name);
		ApplicationEntity application = this.applicationDAO
				.getApplication(name);
		application.setDescription(description);
	}

	@RolesAllowed(SafeOnlineConstants.OPERATOR_ROLE)
	public void registerApplicationOwner(String login)
			throws SubjectNotFoundException, ApplicationNotFoundException,
			AlreadySubscribedException {
		LOG.debug("register application owner: " + login);
		SubjectEntity subject = this.subjectDAO.getSubject(login);
		ApplicationEntity ownerApplication = this.applicationDAO
				.getApplication(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);
		SubscriptionEntity previousSubscription = this.subscriptionDAO
				.findSubscription(subject, ownerApplication);
		if (null != previousSubscription) {
			throw new AlreadySubscribedException();
		}
		this.subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION,
				subject, ownerApplication);
	}

	@RolesAllowed(SafeOnlineConstants.OPERATOR_ROLE)
	public List<String> getApplicationOwners() {
		LOG.debug("get application owners");
		List<String> applicationOwners = new LinkedList<String>();
		ApplicationEntity ownerApplication = this.applicationDAO
				.findApplication(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);
		if (null == ownerApplication) {
			return applicationOwners;
		}
		List<SubscriptionEntity> subscriptions = this.subscriptionDAO
				.getSubscriptions(ownerApplication);
		for (SubscriptionEntity subscription : subscriptions) {
			String login = subscription.getSubject().getLogin();
			applicationOwners.add(login);
		}
		return applicationOwners;
	}
}
