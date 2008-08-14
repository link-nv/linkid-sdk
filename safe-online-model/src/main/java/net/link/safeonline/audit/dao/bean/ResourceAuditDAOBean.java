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
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.ResourceAuditEntity;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;
import net.link.safeonline.jpa.QueryObjectFactory;


@Stateless
public class ResourceAuditDAOBean implements ResourceAuditDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                      entityManager;

    private ResourceAuditEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                ResourceAuditEntity.QueryInterface.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addResourceAudit(AuditContextEntity auditContext, ResourceNameType resourceName,
            ResourceLevelType resourceLevel, String sourceComponent, String message) {

        ResourceAuditEntity resourceAudit = new ResourceAuditEntity(auditContext, resourceName, resourceLevel,
                sourceComponent, message);
        this.entityManager.persist(resourceAudit);
    }

    public void cleanup(Long id) {

        this.queryObject.deleteRecords(id);
    }

    public List<ResourceAuditEntity> listRecords(Long id) {

        return this.queryObject.listRecords(id);
    }

    public List<ResourceAuditEntity> listRecordsSince(Date ageLimit) {

        return this.queryObject.listRecordsSince(ageLimit);
    }

    public List<ResourceAuditEntity> listRecords() {

        return this.queryObject.listRecords();
    }

    public boolean hasRecords(long id) {

        long count = this.queryObject.countRecords(id);
        
        return 0 != count;
    }
}
