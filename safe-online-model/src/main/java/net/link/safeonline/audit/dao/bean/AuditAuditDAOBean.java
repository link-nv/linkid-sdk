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

@Stateless
public class AuditAuditDAOBean implements AuditAuditDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private AuditAuditEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, AuditAuditEntity.QueryInterface.class);
	}

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

	public void cleanup(Long id) {
		this.queryObject.deleteRecords(id);
	}

	public List<AuditAuditEntity> listRecords(Long id) {
		return this.queryObject.listRecords(id);
	}

	public List<AuditAuditEntity> listRecordsSince(Date ageLimit) {
		return this.queryObject.listRecordsSince(ageLimit);
	}

	public boolean hasRecords(long id) {
		long count = this.queryObject.countRecords(id);
		return 0 != count;
	}
}
