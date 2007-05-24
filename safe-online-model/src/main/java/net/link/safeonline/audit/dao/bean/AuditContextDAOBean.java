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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.entity.audit.AuditContextEntity;

@Stateless
public class AuditContextDAOBean implements AuditContextDAO {

	private static final Log LOG = LogFactory.getLog(AuditContextDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public AuditContextEntity createAuditContext() {
		AuditContextEntity auditContext = new AuditContextEntity();
		this.entityManager.persist(auditContext);
		LOG.debug("created audit context: " + auditContext.getId());
		return auditContext;
	}

	public AuditContextEntity getAuditContext(long auditContextId)
			throws AuditContextNotFoundException {
		AuditContextEntity auditContext = this.entityManager.find(
				AuditContextEntity.class, auditContextId);
		if (null == auditContext) {
			throw new AuditContextNotFoundException();
		}
		return auditContext;
	}
}
