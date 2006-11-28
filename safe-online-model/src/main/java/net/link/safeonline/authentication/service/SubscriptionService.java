package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubscriptionEntity;

@Local
public interface SubscriptionService {

	List<SubscriptionEntity> getSubscriptions(String login)
			throws EntityNotFoundException;

	List<ApplicationEntity> getApplications();

	void subscribe(String login, String applicationName)
			throws ApplicationNotFoundException, EntityNotFoundException;

	void unsubscribe(String login, String applicationName)
			throws ApplicationNotFoundException, EntityNotFoundException,
			SubscriptionNotFoundException;
}
