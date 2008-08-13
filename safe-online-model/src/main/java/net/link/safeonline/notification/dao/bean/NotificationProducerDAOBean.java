/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.notification.dao.NotificationProducerDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class NotificationProducerDAOBean implements NotificationProducerDAO {

    private static final Log                                      LOG = LogFactory
                                                                              .getLog(NotificationProducerDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                                         entityManager;

    private NotificationProducerSubscriptionEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                NotificationProducerSubscriptionEntity.QueryInterface.class);
    }

    public NotificationProducerSubscriptionEntity addSubscription(String topic) {

        LOG.debug("add subscription for topic " + topic);
        NotificationProducerSubscriptionEntity subscription = new NotificationProducerSubscriptionEntity(topic);
        this.entityManager.persist(subscription);
        return subscription;
    }

    public NotificationProducerSubscriptionEntity findSubscription(String topic) {

        LOG.debug("find subscription: " + topic);
        return this.entityManager.find(NotificationProducerSubscriptionEntity.class, topic);
    }

    public NotificationProducerSubscriptionEntity getSubscription(String topic) throws SubscriptionNotFoundException {

        LOG.debug("get subscription: " + topic);
        NotificationProducerSubscriptionEntity subscription = findSubscription(topic);
        if (null == subscription)
            throw new SubscriptionNotFoundException();
        return subscription;
    }

    public List<NotificationProducerSubscriptionEntity> listTopics() {

        LOG.debug("list topics");
        return this.queryObject.listTopics();
    }

}
