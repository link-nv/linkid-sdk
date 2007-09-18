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
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.ResourceAuditEntity;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;

@Stateless
public class ResourceAuditDAOBean implements ResourceAuditDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private ResourceAuditEntity.QueryInterface queryObject;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addResourceAudit(AuditContextEntity auditContext,
			ResourceNameType resourceName, ResourceLevelType resourceLevel,
			String sourceComponent, String message) {
		ResourceAuditEntity resourceAudit = new ResourceAuditEntity(
				auditContext, resourceName, resourceLevel, sourceComponent,
				message);
		this.entityManager.persist(resourceAudit);
	}

	public void cleanup(Long id) {
		this.queryObject.deleteRecords(id);
	}

}
