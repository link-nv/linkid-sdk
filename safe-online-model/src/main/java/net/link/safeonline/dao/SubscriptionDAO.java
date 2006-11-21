package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionEntity;

@Local
public interface SubscriptionDAO {

	SubscriptionEntity findSubscription(EntityEntity entity,
			ApplicationEntity application);

	void addSubscription(EntityEntity entity, ApplicationEntity application);
}
