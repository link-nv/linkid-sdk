/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.ApplicationOwnerManager;

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

	@EJB
	private ApplicationOwnerDAO applicationOwnerDAO;

	@EJB
	private ApplicationOwnerManager applicationOwnerManager;

	@PermitAll
	public List<ApplicationEntity> getApplications() {
		// XXX: we're passing the admin subjects with password here!
		List<ApplicationEntity> applications = this.applicationDAO
				.getApplications();
		return applications;
	}

	@RolesAllowed(SafeOnlineConstants.OPERATOR_ROLE)
	public void addApplication(String name, String applicationOwnerName,
			String description) throws ExistingApplicationException,
			ApplicationOwnerNotFoundException {
		LOG.debug("add application: " + name);
		ApplicationEntity existingApplication = this.applicationDAO
				.findApplication(name);
		if (null != existingApplication) {
			throw new ExistingApplicationException();
		}

		ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO
				.getApplicationOwner(applicationOwnerName);

		this.applicationDAO.addApplication(name, applicationOwner, description);
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
	public void registerApplicationOwner(String name, String login)
			throws SubjectNotFoundException, ApplicationNotFoundException,
			ExistingApplicationOwnerException {
		LOG.debug("register application owner: " + name + " with account "
				+ login);
		ApplicationOwnerEntity existingApplicationOwner = this.applicationOwnerDAO
				.findApplicationOwner(name);
		if (null != existingApplicationOwner) {
			throw new ExistingApplicationOwnerException();
		}

		SubjectEntity subject = this.subjectDAO.getSubject(login);

		this.applicationOwnerDAO.addApplicationOwner(name, subject);

		ApplicationEntity ownerApplication = this.applicationDAO
				.getApplication(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);

		this.subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION,
				subject, ownerApplication);
	}

	@RolesAllowed(SafeOnlineConstants.OPERATOR_ROLE)
	public List<ApplicationOwnerEntity> getApplicationOwners() {
		LOG.debug("get application owners");
		// XXX: we're passing the admin subjects with password here!
		List<ApplicationOwnerEntity> applicationOwners = this.applicationOwnerDAO
				.getApplicationOwners();
		return applicationOwners;
	}

	@RolesAllowed(SafeOnlineConstants.OWNER_ROLE)
	public List<ApplicationEntity> getOwnedApplications() {
		LOG.debug("get owned applications");
		ApplicationOwnerEntity applicationOwner = this.applicationOwnerManager
				.getCallerApplicationOwner();
		List<ApplicationEntity> applications = this.applicationDAO
				.getApplications(applicationOwner);
		return applications;
	}
}
