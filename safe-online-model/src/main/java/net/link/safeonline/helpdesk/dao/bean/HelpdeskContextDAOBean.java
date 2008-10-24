/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.dao.HelpdeskContextDAO;
import net.link.safeonline.helpdesk.dao.HelpdeskEventDAO;
import net.link.safeonline.helpdesk.exception.HelpdeskContextNotFoundException;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class HelpdeskContextDAOBean implements HelpdeskContextDAO {

    @EJB
    private HelpdeskEventDAO                     helpdeskEventDAO;

    private static final Log                     LOG = LogFactory.getLog(HelpdeskContextDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                        entityManager;

    private HelpdeskContextEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, HelpdeskContextEntity.QueryInterface.class);
    }

    public HelpdeskContextEntity createHelpdeskContext(String location) {

        HelpdeskContextEntity helpdeskContext = new HelpdeskContextEntity(location);
        this.entityManager.persist(helpdeskContext);

        LOG.debug("created helpdesk context: " + helpdeskContext.getId());
        return helpdeskContext;
    }

    public List<HelpdeskContextEntity> listContexts() {

        return this.queryObject.listContexts();
    }

    public void cleanup() {

        List<HelpdeskContextEntity> contexts = listContexts();
        for (HelpdeskContextEntity context : contexts) {
            List<HelpdeskEventEntity> events = this.helpdeskEventDAO.listEvents(context.getId());
            if (events.size() == 0) {
                LOG.debug("remove empty helpdesk context: " + context.getId());
                this.entityManager.remove(context);
            }
        }
    }

    public void removeContext(Long logId) throws HelpdeskContextNotFoundException {

        HelpdeskContextEntity context = this.entityManager.find(HelpdeskContextEntity.class, logId);
        if (null == context)
            throw new HelpdeskContextNotFoundException();

        this.entityManager.remove(context);
    }
}
