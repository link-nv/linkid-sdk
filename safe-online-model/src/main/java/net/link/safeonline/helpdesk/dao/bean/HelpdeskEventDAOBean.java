/* SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.dao.bean;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.dao.HelpdeskEventDAO;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class HelpdeskEventDAOBean implements HelpdeskEventDAO {

    private final static Log                   LOG = LogFactory.getLog(HelpdeskEventDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                      entityManager;

    private HelpdeskEventEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                HelpdeskEventEntity.QueryInterface.class);
    }

    public void persist(List<HelpdeskEventEntity> helpdeskEvents) {

        for (HelpdeskEventEntity event : helpdeskEvents)
            this.entityManager.persist(event);
    }

    public List<HelpdeskEventEntity> listEvents(Long contextId) {

        return this.queryObject.listLogs(contextId);
    }

    public void clearEvents(long ageInMinutes, LogLevelType logLevel) {

        Date ageLimit = new Date(System.currentTimeMillis() - (ageInMinutes * 60 * 1000));
        LOG.debug("clearing helpdesk " + logLevel.toString() + " events older than: " + ageLimit);
        this.queryObject.deleteEvents(ageLimit, logLevel);
    }

    public void removeEvents(Long logId) {

        this.queryObject.deleteEvents(logId);
    }

    public List<HelpdeskContextEntity> listUserContexts(String user) {

        return this.queryObject.listUserContexts(user);
    }

    public List<String> listUsers() {

        return this.queryObject.listUsers();
    }
}
