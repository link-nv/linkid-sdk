/* SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.dao.HelpdeskEventDAO;
import net.link.safeonline.jpa.QueryObjectFactory;

@Stateless
public class HelpdeskEventDAOBean implements HelpdeskEventDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private HelpdeskEventEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, HelpdeskEventEntity.QueryInterface.class);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addHelpdeskEvent(HelpdeskEventEntity helpdeskEvent) {
		this.entityManager.persist(helpdeskEvent);
	}

	public List<HelpdeskEventEntity> listLogs(Long contextId) {
		return this.queryObject.listLogs(contextId);
	}
}
