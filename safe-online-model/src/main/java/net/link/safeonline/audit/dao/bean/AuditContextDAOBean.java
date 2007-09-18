/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.dao.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.dao.AccessAuditDAO;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.audit.dao.SecurityAuditDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.entity.audit.AuditContextEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class AuditContextDAOBean implements AuditContextDAO {

	private static final Log LOG = LogFactory.getLog(AuditContextDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@EJB
	AuditAuditDAO auditAuditDAO;

	@EJB
	AccessAuditDAO accessAuditDAO;

	@EJB
	SecurityAuditDAO securityAuditDAO;

	@EJB
	ResourceAuditDAO resourceAuditDAO;

	private AuditContextEntity.QueryInterface queryObject;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public AuditContextEntity createAuditContext() {
		AuditContextEntity auditContext = new AuditContextEntity();
		this.entityManager.persist(auditContext);
		LOG.debug("created audit context: " + auditContext.getId());
		return auditContext;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public AuditContextEntity getAuditContext(long auditContextId)
			throws AuditContextNotFoundException {
		/*
		 * We also put REQUIRES_NEW here, else we risk a 'no transaction open'
		 * exception.
		 */
		AuditContextEntity auditContext = this.entityManager.find(
				AuditContextEntity.class, auditContextId);
		if (null == auditContext) {
			throw new AuditContextNotFoundException();
		}
		return auditContext;
	}

	public void cleanup(long ageInMinutes) {
		Date ageLimit = new Date(((System.currentTimeMillis() / 60) / 1000)
				- ageInMinutes);

		List<AuditContextEntity> contexts = this.queryObject
				.listContexts(ageLimit);
		for (AuditContextEntity context : contexts) {
			auditAuditDAO.cleanup(context.getId());
			accessAuditDAO.cleanup(context.getId());
			securityAuditDAO.cleanup(context.getId());
			resourceAuditDAO.cleanup(context.getId());
			this.entityManager.remove(context);
		}
	}
}
