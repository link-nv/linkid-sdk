/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

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

	SubscriptionEntity getSubscription(SubjectEntity subject,
			ApplicationEntity application) throws SubscriptionNotFoundException;

	void addSubscription(SubscriptionOwnerType subscriptionOwnerType,
			SubjectEntity subject, ApplicationEntity application);

	List<SubscriptionEntity> getSubsciptions(SubjectEntity subject);

	List<SubscriptionEntity> getSubscriptions(ApplicationEntity application);

	void removeSubscription(SubjectEntity subject, ApplicationEntity application)
			throws SubscriptionNotFoundException;

	void removeSubscription(SubscriptionEntity subscriptionEntity);

	long getNumberOfSubscriptions(ApplicationEntity application);
}
