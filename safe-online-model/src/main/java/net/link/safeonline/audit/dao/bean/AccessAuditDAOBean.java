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
import net.link.safeonline.audit.dao.AccessAuditDAO;
import net.link.safeonline.entity.audit.AccessAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.OperationStateType;

@Stateless
public class AccessAuditDAOBean implements AccessAuditDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addAccessAudit(AuditContextEntity auditContext,
			String operation, OperationStateType operationState,
			String principal) {
		AccessAuditEntity accessAudit = new AccessAuditEntity(auditContext,
				operation, operationState, principal);
		this.entityManager.persist(accessAudit);
	}
}
