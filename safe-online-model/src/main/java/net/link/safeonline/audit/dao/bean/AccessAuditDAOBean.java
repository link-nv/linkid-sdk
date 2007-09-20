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
import net.link.safeonline.audit.dao.AccessAuditDAO;
import net.link.safeonline.entity.audit.AccessAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.OperationStateType;
import net.link.safeonline.jpa.QueryObjectFactory;

@Stateless
public class AccessAuditDAOBean implements AccessAuditDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private AccessAuditEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, AccessAuditEntity.QueryInterface.class);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addAccessAudit(AuditContextEntity auditContext,
			String operation, OperationStateType operationState,
			String principal) {
		AccessAuditEntity accessAudit = new AccessAuditEntity(auditContext,
				operation, operationState, principal);
		this.entityManager.persist(accessAudit);
	}

	public void cleanup(Long id) {
		this.queryObject.deleteRecords(id);
	}

	public List<AccessAuditEntity> listRecords(Long id) {
		return this.queryObject.listRecords(id);
	}

	public List<AccessAuditEntity> listRecordsSince(Date ageLimit) {
		return this.queryObject.listRecordsSince(ageLimit);
	}

	public List<String> listUsers() {
		return this.queryObject.listUsers();
	}

	public List<AccessAuditEntity> listRecords(String principal) {
		return this.queryObject.listUserRecords(principal);
	}
}
