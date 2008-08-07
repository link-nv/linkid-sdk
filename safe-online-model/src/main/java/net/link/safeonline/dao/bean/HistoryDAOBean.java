/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.HistoryPropertyEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class HistoryDAOBean implements HistoryDAO {

    private static final Log                     LOG = LogFactory
                                                             .getLog(HistoryDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                        entityManager;

    private HistoryEntity.QueryInterface         queryObject;

    private HistoryPropertyEntity.QueryInterface propertyQueryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(
                this.entityManager, HistoryEntity.QueryInterface.class);
        this.propertyQueryObject = QueryObjectFactory.createQueryObject(
                this.entityManager, HistoryPropertyEntity.QueryInterface.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public HistoryEntity addHistoryEntry(Date when, SubjectEntity subject,
            HistoryEventType event, Map<String, String> properties) {

        LOG.debug("add history entry: " + when + "; subject: "
                + subject.getUserId() + "; event: " + event);
        HistoryEntity history = new HistoryEntity(when, subject, event);
        this.entityManager.persist(history);
        this.entityManager.refresh(history);
        if (null != properties) {
            for (Map.Entry<String, String> property : properties.entrySet()) {
                HistoryPropertyEntity propertyEntity = new HistoryPropertyEntity(
                        history, property.getKey(), property.getValue());
                /*
                 * Manage relationships
                 */
                history.getProperties().put(propertyEntity.getName(),
                        propertyEntity);
                /*
                 * Persist
                 */
                this.entityManager.persist(propertyEntity);
            }
        }
        return history;
    }

    public HistoryEntity addHistoryEntry(SubjectEntity subject,
            HistoryEventType event, Map<String, String> properties) {

        Date when = new Date();
        return addHistoryEntry(when, subject, event, properties);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public HistoryEntity addHExceptionHistoryEntry(Date when,
            SubjectEntity subject, HistoryEventType event,
            Map<String, String> properties) {

        return addHistoryEntry(when, subject, event, properties);
    }

    public List<HistoryEntity> getHistory(SubjectEntity subject) {

        LOG.debug("get history for entity: " + subject.getUserId());
        List<HistoryEntity> result = this.queryObject.getHistory(subject);
        return result;
    }

    public void clearAllHistory(Date ageLimit) {

        LOG.debug("clearing subject history entries older than: " + ageLimit);

        // remove all history properties
        List<HistoryEntity> histories = this.queryObject.getHistory(ageLimit);
        for (HistoryEntity history : histories) {
            List<HistoryPropertyEntity> historyProperties = this.propertyQueryObject
                    .listProperties(history);
            for (HistoryPropertyEntity historyProperty : historyProperties) {
                removeProperty(historyProperty);
            }
        }

        this.entityManager.flush();

        this.queryObject.deleteWhereOlder(ageLimit);
    }

    public void clearAllHistory(SubjectEntity subject) {

        // remove all history properties
        List<HistoryEntity> histories = getHistory(subject);
        for (HistoryEntity history : histories) {
            List<HistoryPropertyEntity> historyProperties = this.propertyQueryObject
                    .listProperties(history);
            for (HistoryPropertyEntity historyProperty : historyProperties) {
                removeProperty(historyProperty);
            }
        }

        this.entityManager.flush();

        this.queryObject.deleteAll(subject);
    }

    private void removeProperty(HistoryPropertyEntity property) {

        /*
         * Manage relationships.
         */
        String name = property.getName();
        property.getHistory().getProperties().remove(name);
        /*
         * Remove from database.
         */
        this.entityManager.remove(property);
    }

}
