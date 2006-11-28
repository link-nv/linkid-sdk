package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.AlreadySubscribedException;
import net.link.safeonline.authentication.service.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.EntityNotFoundException;
import net.link.safeonline.authentication.service.PermissionDeniedException;
import net.link.safeonline.authentication.service.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;

@Stateless
public class SubscriptionServiceBean implements SubscriptionService {

	@EJB
	private EntityDAO entityDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	public List<SubscriptionEntity> getSubscriptions(String login)
			throws EntityNotFoundException {
		/*
		 * XXX: should use a security domain here.
		 */
		EntityEntity entity = this.entityDAO.getEntity(login);
		List<SubscriptionEntity> subscriptions = this.subscriptionDAO
				.getSubsciptions(entity);
		return subscriptions;
	}

	public List<ApplicationEntity> getApplications() {
		/*
		 * Does not require a security domain. Is a public function.
		 */
		List<ApplicationEntity> applications = this.applicationDAO
				.getApplications();
		return applications;
	}

	public void subscribe(String login, String applicationName)
			throws ApplicationNotFoundException, EntityNotFoundException,
			AlreadySubscribedException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		EntityEntity entity = this.entityDAO.getEntity(login);
		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(entity, application);
		if (null != subscription) {
			throw new AlreadySubscribedException();
		}
		this.subscriptionDAO.addSubscription(SubscriptionOwnerType.ENTITY,
				entity, application);
	}

	public void unsubscribe(String login, String applicationName)
			throws ApplicationNotFoundException, EntityNotFoundException,
			SubscriptionNotFoundException, PermissionDeniedException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		EntityEntity entity = this.entityDAO.getEntity(login);
		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(entity, application);
		if (null == subscription) {
			throw new SubscriptionNotFoundException();
		}
		if (!SubscriptionOwnerType.ENTITY.equals(subscription
				.getSubscriptionOwnerType())) {
			throw new PermissionDeniedException();
		}
		this.subscriptionDAO.removeSubscription(entity, application);
	}
}
