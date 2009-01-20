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
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = AuditContextDAO.JNDI_BINDING)
public class AuditContextDAOBean implements AuditContextDAO {

    private static final Log                  LOG = LogFactory.getLog(AuditContextDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                     entityManager;

    @EJB(mappedName = AuditAuditDAO.JNDI_BINDING)
    AuditAuditDAO                             auditAuditDAO;

    @EJB(mappedName = AccessAuditDAO.JNDI_BINDING)
    AccessAuditDAO                            accessAuditDAO;

    @EJB(mappedName = SecurityAuditDAO.JNDI_BINDING)
    SecurityAuditDAO                          securityAuditDAO;

    @EJB(mappedName = ResourceAuditDAO.JNDI_BINDING)
    ResourceAuditDAO                          resourceAuditDAO;

    private AuditContextEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, AuditContextEntity.QueryInterface.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public AuditContextEntity createAuditContext() {

        AuditContextEntity auditContext = new AuditContextEntity();
        entityManager.persist(auditContext);

        LOG.debug("created audit context: " + auditContext.getId());
        return auditContext;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public AuditContextEntity getAuditContext(long auditContextId)
            throws AuditContextNotFoundException {

        /*
         * We also put REQUIRES_NEW here, otherwise we risk a 'no transaction open' exception.
         * 
         * (FIXME: What's wrong with a REQUIRED? -mbillemo)
         */
        AuditContextEntity auditContext = entityManager.find(AuditContextEntity.class, auditContextId);
        if (null == auditContext)
            throw new AuditContextNotFoundException();

        return auditContext;
    }

    public void cleanup(long ageInMinutes) {

        Date ageLimit = new Date(System.currentTimeMillis() - ageInMinutes * 60 * 1000);
        LOG.debug("Cleaning audit contexts older then " + ageLimit);

        List<AuditContextEntity> contexts = queryObject.listContextsOlderThen(ageLimit);
        for (AuditContextEntity context : contexts) {
            LOG.debug("Cleaning context " + context.getId());

            auditAuditDAO.cleanup(context.getId());
            accessAuditDAO.cleanup(context.getId());
            securityAuditDAO.cleanup(context.getId());
            resourceAuditDAO.cleanup(context.getId());
            entityManager.remove(context);
        }
    }

    public List<AuditContextEntity> listContexts() {

        return queryObject.listContexts();
    }

    public boolean removeAuditContext(Long auditContextId)
            throws AuditContextNotFoundException {

        AuditContextEntity auditContext = getAuditContext(auditContextId);
        entityManager.remove(auditContext);

        return true;
    }

    public List<AuditContextEntity> listLastContexts() {

        return queryObject.listLastContexts();
    }
}
