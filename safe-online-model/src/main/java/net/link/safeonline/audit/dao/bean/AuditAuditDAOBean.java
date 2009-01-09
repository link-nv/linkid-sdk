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
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.entity.audit.AuditAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = AuditAuditDAO.JNDI_BINDING)
public class AuditAuditDAOBean implements AuditAuditDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                   entityManager;

    private AuditAuditEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, AuditAuditEntity.QueryInterface.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addAuditAudit(AuditContextEntity auditContext, String message) {

        AuditAuditEntity auditAudit = new AuditAuditEntity(auditContext, message);
        entityManager.persist(auditAudit);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addAuditAudit(String message) {

        AuditAuditEntity auditAudit = new AuditAuditEntity(message);
        entityManager.persist(auditAudit);
    }

    public void cleanup(Long id) {

        queryObject.deleteRecords(id);
    }

    public List<AuditAuditEntity> listRecords(Long id) {

        return queryObject.listRecords(id);
    }

    public List<AuditAuditEntity> listRecordsSince(Date ageLimit) {

        return queryObject.listRecordsSince(ageLimit);
    }

    public boolean hasRecords(long id) {

        long count = queryObject.countRecords(id);

        return 0 != count;
    }

    public List<AuditAuditEntity> listRecords() {

        return queryObject.listRecords();
    }
}
