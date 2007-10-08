/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.entity.SubscriptionPK;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class SubscriptionDAOBean implements SubscriptionDAO {

	private static final Log LOG = LogFactory.getLog(SubscriptionDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private SubscriptionEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, SubscriptionEntity.QueryInterface.class);
	}

	@SuppressWarnings("unchecked")
	public SubscriptionEntity findSubscription(SubjectEntity subject,
			ApplicationEntity application) {
		LOG.debug("find subscription for: " + subject.getUserId() + " to "
				+ application.getName());
		SubscriptionPK subscriptionPK = new SubscriptionPK(subject, application);
		SubscriptionEntity subscription = this.entityManager.find(
				SubscriptionEntity.class, subscriptionPK);
		return subscription;
	}

	public void addSubscription(SubscriptionOwnerType subscriptionOwnerType,
			SubjectEntity subject, ApplicationEntity application) {
		LOG.debug("add subscription for " + subject.getUserId() + " to "
				+ application.getName());
		SubscriptionEntity subscription = new SubscriptionEntity(
				subscriptionOwnerType, subject, application);
		this.entityManager.persist(subscription);
	}

	public List<SubscriptionEntity> listSubsciptions(SubjectEntity subject) {
		LOG.debug("get subscriptions for subject: " + subject.getUserId());
		List<SubscriptionEntity> subscriptions = this.queryObject
				.listSubsciptions(subject);
		return subscriptions;
	}

	public void removeSubscription(SubjectEntity subject,
			ApplicationEntity application) throws SubscriptionNotFoundException {
		SubscriptionEntity subscription = findSubscription(subject, application);
		if (null == subscription) {
			throw new SubscriptionNotFoundException();
		}
		this.entityManager.remove(subscription);
	}

	public long getNumberOfSubscriptions(ApplicationEntity application) {
		long countResult = this.queryObject
				.getNumberOfSubscriptions(application);
		return countResult;
	}

	public List<SubscriptionEntity> listSubscriptions(
			ApplicationEntity application) {
		LOG
				.debug("get subscriptions for application: "
						+ application.getName());
		List<SubscriptionEntity> subscriptions = this.queryObject
				.listSubscriptions(application);
		return subscriptions;
	}

	public void removeSubscription(SubscriptionEntity subscriptionEntity) {
		LOG.debug("remove subscription: " + subscriptionEntity);
		this.entityManager.remove(subscriptionEntity);
	}

	public SubscriptionEntity getSubscription(SubjectEntity subject,
			ApplicationEntity application) throws SubscriptionNotFoundException {
		SubscriptionEntity subscription = findSubscription(subject, application);
		if (null == subscription) {
			throw new SubscriptionNotFoundException();
		}
		return subscription;
	}

	public long getActiveNumberOfSubscriptions(ApplicationEntity application,
			long activeLimitInMillis) {
		Query query = SubscriptionEntity
				.createQueryCountWhereApplicationAndActive(this.entityManager,
						application, activeLimitInMillis);
		Long countResult = (Long) query.getSingleResult();
		return countResult;
	}

	public void loggedIn(SubscriptionEntity subscription) {
		subscription.setLastLogin(new Date());
	}

	public void removeAllSubscriptions(SubjectEntity subject) {
		this.queryObject.deleteAll(subject);
	}
}
