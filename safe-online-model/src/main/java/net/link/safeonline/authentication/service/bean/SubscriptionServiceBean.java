package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.SubjectManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class SubscriptionServiceBean implements SubscriptionService {

	private static final Log LOG = LogFactory
			.getLog(SubscriptionServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
	public List<SubscriptionEntity> getSubscriptions() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		List<SubscriptionEntity> subscriptions = this.subscriptionDAO
				.getSubsciptions(subject);
		return subscriptions;
	}

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
	public void subscribe(String applicationName)
			throws ApplicationNotFoundException, AlreadySubscribedException,
			PermissionDeniedException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		if (!application.isAllowUserSubscription()) {
			throw new PermissionDeniedException();
		}
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(subject, application);
		if (null != subscription) {
			throw new AlreadySubscribedException();
		}
		this.subscriptionDAO.addSubscription(SubscriptionOwnerType.SUBJECT,
				subject, application);
	}

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
	public void unsubscribe(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			PermissionDeniedException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(subject, application);
		if (null == subscription) {
			throw new SubscriptionNotFoundException();
		}
		if (!SubscriptionOwnerType.SUBJECT.equals(subscription
				.getSubscriptionOwnerType())) {
			throw new PermissionDeniedException();
		}
		this.subscriptionDAO.removeSubscription(subject, application);
	}

	@RolesAllowed(SafeOnlineConstants.OPERATOR_ROLE)
	public long getNumberOfSubscriptions(String applicationName)
			throws ApplicationNotFoundException {
		LOG.debug("get number of subscriptions for application: "
				+ applicationName);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		long count = this.subscriptionDAO.getNumberOfSubscriptions(application);
		return count;
	}
}
