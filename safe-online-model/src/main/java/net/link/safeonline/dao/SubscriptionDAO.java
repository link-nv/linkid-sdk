package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;

/**
 * Subcription entity data access object interface definition.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface SubscriptionDAO {

	SubscriptionEntity findSubscription(SubjectEntity subject,
			ApplicationEntity application);

	void addSubscription(SubscriptionOwnerType subscriptionOwnerType,
			SubjectEntity subject, ApplicationEntity application);

	List<SubscriptionEntity> getSubsciptions(SubjectEntity subject);

	void removeSubscription(SubjectEntity subject, ApplicationEntity application)
			throws SubscriptionNotFoundException;
}
