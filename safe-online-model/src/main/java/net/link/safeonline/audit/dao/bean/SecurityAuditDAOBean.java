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
import net.link.safeonline.audit.dao.SecurityAuditDAO;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.SecurityAuditEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.jpa.QueryObjectFactory;


@Stateless
public class SecurityAuditDAOBean implements SecurityAuditDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                      entityManager;

    private SecurityAuditEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                SecurityAuditEntity.QueryInterface.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addSecurityAudit(AuditContextEntity auditContext, SecurityThreatType securityThreat,
            String targetPrincipal, String message) {

        SecurityAuditEntity securityAudit = new SecurityAuditEntity(auditContext, securityThreat, targetPrincipal,
                message);
        this.entityManager.persist(securityAudit);
    }

    public void cleanup(Long id) {

        this.queryObject.deleteRecords(id);
    }

    public List<SecurityAuditEntity> listRecords(Long id) {

        return this.queryObject.listRecords(id);
    }

    public List<SecurityAuditEntity> listRecordsSince(Date ageLimit) {

        return this.queryObject.listRecordsSince(ageLimit);
    }

    public List<SecurityAuditEntity> listRecords() {

        return this.queryObject.listRecords();
    }

    public List<String> listUsers() {

        return this.queryObject.listUsers();
    }

    public List<SecurityAuditEntity> listRecords(String principal) {

        return this.queryObject.listUserRecords(principal);
    }

    public boolean hasRecords(long id) {

        long count = this.queryObject.countRecords(id);
        
        return 0 != count;
    }
}
