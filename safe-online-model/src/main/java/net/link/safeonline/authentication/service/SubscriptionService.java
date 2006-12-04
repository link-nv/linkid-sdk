package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubscriptionEntity;

@Local
public interface SubscriptionService {

	List<SubscriptionEntity> getSubscriptions();

	List<ApplicationEntity> getApplications();

	void subscribe(String applicationName) throws ApplicationNotFoundException,
			AlreadySubscribedException;

	void unsubscribe(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			PermissionDeniedException;
}
