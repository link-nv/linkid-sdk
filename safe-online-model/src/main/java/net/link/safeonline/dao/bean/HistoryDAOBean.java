/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class HistoryDAOBean implements HistoryDAO {

	private static final Log LOG = LogFactory.getLog(HistoryDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private HistoryEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, HistoryEntity.QueryInterface.class);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addHistoryEntry(Date when, SubjectEntity subject, String event) {
		LOG.debug("add history entry: " + when + "; subject: "
				+ subject.getLogin() + "; event: " + event);
		HistoryEntity history = new HistoryEntity(when, subject, event);
		this.entityManager.persist(history);
	}

	public List<HistoryEntity> getHistory(SubjectEntity subject) {
		LOG.debug("get history for entity: " + subject.getLogin());
		List<HistoryEntity> result = this.queryObject.getHistory(subject);
		return result;
	}

	public void clearAllHistory(long ageInMillis) {
		LOG
				.debug("clearing subject history entries older than: "
						+ ageInMillis);
		Query query = HistoryEntity.createQueryDeleteWhereOlder(
				this.entityManager, ageInMillis);
		query.executeUpdate();
	}
}
