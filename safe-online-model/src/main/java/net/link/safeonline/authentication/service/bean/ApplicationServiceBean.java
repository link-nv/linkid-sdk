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
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubscriptionEntity;

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
}
