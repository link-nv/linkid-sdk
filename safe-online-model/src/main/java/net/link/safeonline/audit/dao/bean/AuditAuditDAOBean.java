/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.dao.bean;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.entity.audit.AuditAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;

@Stateless
public class AuditAuditDAOBean implements AuditAuditDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addAuditAudit(AuditContextEntity auditContext, String message) {
		AuditAuditEntity auditAudit = new AuditAuditEntity(auditContext,
				message);
		this.entityManager.persist(auditAudit);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addAuditAudit(String message) {
		AuditAuditEntity auditAudit = new AuditAuditEntity(message);
		this.entityManager.persist(auditAudit);
	}
}
