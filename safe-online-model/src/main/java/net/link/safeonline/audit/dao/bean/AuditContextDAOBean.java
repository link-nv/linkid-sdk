/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.dao.bean;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.dao.AccessAuditDAO;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.audit.dao.SecurityAuditDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class AuditContextDAOBean implements AuditContextDAO {

    private static final Log                  LOG = LogFactory.getLog(AuditContextDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                     entityManager;

    @EJB
    AuditAuditDAO                             auditAuditDAO;

    @EJB
    AccessAuditDAO                            accessAuditDAO;

    @EJB
    SecurityAuditDAO                          securityAuditDAO;

    @EJB
    ResourceAuditDAO                          resourceAuditDAO;

    private AuditContextEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, AuditContextEntity.QueryInterface.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public AuditContextEntity createAuditContext() {

        AuditContextEntity auditContext = new AuditContextEntity();
        this.entityManager.persist(auditContext);

        LOG.debug("created audit context: " + auditContext.getId());
        return auditContext;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public AuditContextEntity getAuditContext(long auditContextId) throws AuditContextNotFoundException {

        /*
         * We also put REQUIRES_NEW here, otherwise we risk a 'no transaction open' exception.
         * 
         * (FIXME: What's wrong with a REQUIRED? -mbillemo)
         */
        AuditContextEntity auditContext = this.entityManager.find(AuditContextEntity.class, auditContextId);
        if (null == auditContext)
            throw new AuditContextNotFoundException();

        return auditContext;
    }

    public void cleanup(long ageInMinutes) {

        Date ageLimit = new Date(System.currentTimeMillis() - ageInMinutes * 60 * 1000);
        LOG.debug("Cleaning audit contexts older then " + ageLimit);

        List<AuditContextEntity> contexts = this.queryObject.listContextsOlderThen(ageLimit);
        for (AuditContextEntity context : contexts) {
            LOG.debug("Cleaning context " + context.getId());

            this.auditAuditDAO.cleanup(context.getId());
            this.accessAuditDAO.cleanup(context.getId());
            this.securityAuditDAO.cleanup(context.getId());
            this.resourceAuditDAO.cleanup(context.getId());
            this.entityManager.remove(context);
        }
    }

    public List<AuditContextEntity> listContexts() {

        return this.queryObject.listContexts();
    }

    public boolean removeAuditContext(Long auditContextId) throws AuditContextNotFoundException {

        AuditContextEntity auditContext = getAuditContext(auditContextId);
        this.entityManager.remove(auditContext);

        return true;
    }

    public List<AuditContextEntity> listLastContexts() {

        return this.queryObject.listLastContexts();
    }
}
