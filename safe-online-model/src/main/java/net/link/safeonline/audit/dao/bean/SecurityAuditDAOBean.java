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

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = SecurityAuditDAO.JNDI_BINDING)
public class SecurityAuditDAOBean implements SecurityAuditDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                      entityManager;

    private SecurityAuditEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, SecurityAuditEntity.QueryInterface.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addSecurityAudit(AuditContextEntity auditContext, SecurityThreatType securityThreat, String targetPrincipal, String message) {

        SecurityAuditEntity securityAudit = new SecurityAuditEntity(auditContext, securityThreat, targetPrincipal, message);
        entityManager.persist(securityAudit);
    }

    public void cleanup(Long id) {

        queryObject.deleteRecords(id);
    }

    public List<SecurityAuditEntity> listRecords(Long id) {

        return queryObject.listRecords(id);
    }

    public List<SecurityAuditEntity> listRecordsSince(Date ageLimit) {

        return queryObject.listRecordsSince(ageLimit);
    }

    public List<SecurityAuditEntity> listRecords() {

        return queryObject.listRecords();
    }

    public List<String> listUsers() {

        return queryObject.listUsers();
    }

    public List<SecurityAuditEntity> listRecords(String principal) {

        return queryObject.listUserRecords(principal);
    }

    public boolean hasRecords(long id) {

        long count = queryObject.countRecords(id);

        return 0 != count;
    }
}
