package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.service.SubscriptionNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;

@Local
public interface SubscriptionDAO {

	SubscriptionEntity findSubscription(EntityEntity entity,
			ApplicationEntity application);

	void addSubscription(SubscriptionOwnerType subscriptionOwnerType,
			EntityEntity entity, ApplicationEntity application);

	List<SubscriptionEntity> getSubsciptions(EntityEntity entity);

	void removeSubscription(EntityEntity entity, ApplicationEntity application)
			throws SubscriptionNotFoundException;
}
