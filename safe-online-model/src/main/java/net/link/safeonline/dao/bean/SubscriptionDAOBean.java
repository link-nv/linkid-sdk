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
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.entity.SubscriptionPK;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.model.IdGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class SubscriptionDAOBean implements SubscriptionDAO {

    private static final Log                  LOG = LogFactory.getLog(SubscriptionDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                     entityManager;

    private SubscriptionEntity.QueryInterface queryObject;

    @EJB
    private IdGenerator                       idGenerator;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                SubscriptionEntity.QueryInterface.class);
    }

    public SubscriptionEntity findSubscription(SubjectEntity subject, ApplicationEntity application) {

        LOG.debug("find subscription for: " + subject.getUserId() + " to " + application.getName());
        SubscriptionPK subscriptionPK = new SubscriptionPK(subject, application);
        SubscriptionEntity subscription = this.entityManager.find(SubscriptionEntity.class, subscriptionPK);
        return subscription;
    }

    public void addSubscription(SubscriptionOwnerType subscriptionOwnerType, SubjectEntity subject,
            ApplicationEntity application) {

        String subscriptionUserId = this.idGenerator.generateId();
        LOG.debug("add subscription for " + subject.getUserId() + " to " + application.getName()
                + "  subscriptionUserId = " + subscriptionUserId);
        SubscriptionEntity subscription = new SubscriptionEntity(subscriptionOwnerType, subject, subscriptionUserId,
                application);
        this.entityManager.persist(subscription);
    }

    public void addSubscription(SubscriptionOwnerType subscriptionOwnerType, SubjectEntity subject,
            ApplicationEntity application, String subscriptionUserId) {

        LOG.debug("add subscription for " + subject.getUserId() + " to " + application.getName()
                + "  subscriptionUserId = " + subscriptionUserId);
        SubscriptionEntity subscription = new SubscriptionEntity(subscriptionOwnerType, subject, subscriptionUserId,
                application);
        this.entityManager.persist(subscription);
    }

    public List<SubscriptionEntity> listSubsciptions(SubjectEntity subject) {

        LOG.debug("get subscriptions for subject: " + subject.getUserId());
        List<SubscriptionEntity> subscriptions = this.queryObject.listSubsciptions(subject);
        return subscriptions;
    }

    public void removeSubscription(SubjectEntity subject, ApplicationEntity application)
            throws SubscriptionNotFoundException {

        SubscriptionEntity subscription = findSubscription(subject, application);
        if (null == subscription)
            throw new SubscriptionNotFoundException();
        this.entityManager.remove(subscription);
    }

    public long getNumberOfSubscriptions(ApplicationEntity application) {

        long countResult = this.queryObject.getNumberOfSubscriptions(application);
        return countResult;
    }

    public List<SubscriptionEntity> listSubscriptions(ApplicationEntity application) {

        LOG.debug("get subscriptions for application: " + application.getName());
        List<SubscriptionEntity> subscriptions = this.queryObject.listSubscriptions(application);
        return subscriptions;
    }

    public void removeSubscription(SubscriptionEntity subscriptionEntity) {

        LOG.debug("remove subscription: " + subscriptionEntity);
        this.entityManager.remove(subscriptionEntity);
    }

    public SubscriptionEntity getSubscription(SubjectEntity subject, ApplicationEntity application)
            throws SubscriptionNotFoundException {

        SubscriptionEntity subscription = findSubscription(subject, application);
        if (null == subscription)
            throw new SubscriptionNotFoundException();
        return subscription;
    }

    public long getActiveNumberOfSubscriptions(ApplicationEntity application, Date activeLimit) {

        return this.queryObject.getNumberOfActiveSubscriptions(application, activeLimit);
    }

    public void loggedIn(SubscriptionEntity subscription) {

        subscription.setLastLogin(new Date());
    }

    public void removeAllSubscriptions(SubjectEntity subject) {

        this.queryObject.deleteAll(subject);
    }

    public SubscriptionEntity findSubscription(String subscriptionUserId) {

        LOG.debug("get subscriptions for : " + subscriptionUserId);
        return this.queryObject.findSubscription(subscriptionUserId);
    }
}
