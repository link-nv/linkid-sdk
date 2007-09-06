/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.helpdesk.dao.HelpdeskContextDAO;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class HelpdeskContextDAOBean implements HelpdeskContextDAO {

	private static final Log LOG = LogFactory
			.getLog(HelpdeskContextDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private HelpdeskContextEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, HelpdeskContextEntity.QueryInterface.class);
	}

	public HelpdeskContextEntity createHelpdeskContext() {
		HelpdeskContextEntity helpdeskContext = new HelpdeskContextEntity();
		this.entityManager.persist(helpdeskContext);
		LOG.debug("created helpdesk context: " + helpdeskContext.getId());
		return helpdeskContext;
	}

	public List<HelpdeskContextEntity> listContexts() {
		return this.queryObject.listContexts();
	}

}
