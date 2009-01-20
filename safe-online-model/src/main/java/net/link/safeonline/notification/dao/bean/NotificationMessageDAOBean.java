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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.NotificationMessageNotFoundException;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationMessageEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.notification.dao.NotificationMessageDAO;
import net.link.safeonline.notification.message.NotificationMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = NotificationMessageDAO.JNDI_BINDING)
public class NotificationMessageDAOBean implements NotificationMessageDAO {

    private static final Log                         LOG = LogFactory.getLog(NotificationMessageDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                            entityManager;

    private NotificationMessageEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, NotificationMessageEntity.QueryInterface.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public NotificationMessageEntity addNotificationMessage(NotificationMessage notificationMessage, EndpointReferenceEntity consumer) {

        LOG.debug("persist notification message");
        NotificationMessageEntity notificationMessageEntity = new NotificationMessageEntity(notificationMessage.getTopic(), consumer,
                notificationMessage.getSubject(), notificationMessage.getContent());
        entityManager.persist(notificationMessageEntity);
        return notificationMessageEntity;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public NotificationMessageEntity findNotificationMessage(NotificationMessage message, EndpointReferenceEntity consumer) {

        LOG.debug("find notification message topic: " + message.getTopic() + " subject: " + message.getSubject() + " content: "
                + message.getContent() + " consumerId: " + consumer.getId());
        if (null == message.getSubject() && null == message.getContent())
            return queryObject.findNotificationMessage(message.getTopic(), consumer);
        else if (null == message.getSubject())
            return queryObject.findNotificationMessageWhereContent(message.getTopic(), consumer, message.getContent());
        else if (null == message.getContent())
            return queryObject.findNotificationMessageWhereSubject(message.getTopic(), consumer, message.getSubject());
        else
            return queryObject.findNotificationMessage(message.getTopic(), consumer, message.getSubject(), message.getContent());
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addNotificationAttempt(NotificationMessage message, EndpointReferenceEntity consumer) {

        NotificationMessageEntity notificationMessage = findNotificationMessage(message, consumer);
        if (null == notificationMessage) {
            addNotificationMessage(message, consumer);
        } else {
            notificationMessage.addAttempt();
            LOG.debug("attempts: " + notificationMessage.getAttempts());
        }
    }

    public NotificationMessageEntity getNotificationMessage(NotificationMessage notificationMessage, EndpointReferenceEntity consumer)
            throws NotificationMessageNotFoundException {

        LOG.debug("get notification message");
        NotificationMessageEntity notificationMessageEntity = findNotificationMessage(notificationMessage, consumer);
        if (null == notificationMessageEntity)
            throw new NotificationMessageNotFoundException();
        return notificationMessageEntity;
    }

    public void removeNotificationMessage(NotificationMessageEntity notificationMessageEntity) {

        LOG.debug("remove notification message");
        entityManager.remove(notificationMessageEntity);

    }

    @SuppressWarnings("unchecked")
    public List<NotificationMessageEntity> listNotificationMessages() {

        LOG.debug("list notification messages");
        return queryObject.listNotificationMessages();
    }

}
