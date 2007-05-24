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
import net.link.safeonline.audit.dao.SecurityAuditDAO;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.SecurityAuditEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;

@Stateless
public class SecurityAuditDAOBean implements SecurityAuditDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addSecurityAudit(AuditContextEntity auditContext,
			SecurityThreatType securityThreat, String targetPrincipal,
			String message) {
		SecurityAuditEntity securityAudit = new SecurityAuditEntity(
				auditContext, securityThreat, targetPrincipal, message);
		this.entityManager.persist(securityAudit);
	}
}
