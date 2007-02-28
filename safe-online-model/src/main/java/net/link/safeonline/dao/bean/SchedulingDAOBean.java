/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TimerHandle;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.SchedulingDAO;
import net.link.safeonline.entity.SchedulingEntity;

@Stateless
public class SchedulingDAOBean implements SchedulingDAO {

	private static final Log LOG = LogFactory.getLog(SchedulingDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public SchedulingEntity addScheduling(String name, String cronExpression) {
		LOG.debug("Adding SchedulingEntity: " + name + " at " + cronExpression);

		SchedulingEntity schedulingEntity = new SchedulingEntity(name,
				cronExpression, null);
		this.entityManager.persist(schedulingEntity);
		return schedulingEntity;
	}

	public void removeScheduling(String name) {
		LOG.debug("Removing SchedulingEntity: " + name);

		SchedulingEntity schedulingEntity = this.entityManager.find(
				SchedulingEntity.class, name);
		this.entityManager.remove(schedulingEntity);
	}

	@SuppressWarnings("unchecked")
	public SchedulingEntity findSchedulingByName(String name) {
		LOG.debug("Looking for scheduling by name: " + name);

		Query query = SchedulingEntity.createQueryWhereName(this.entityManager,
				name);
		List<SchedulingEntity> result = query.getResultList();
		if (result.isEmpty()) {
			return null;
		}
		return result.get(0);
	}

	@SuppressWarnings("unchecked")
	public SchedulingEntity findSchedulingByTimerHandle(TimerHandle timerHandle) {
		LOG.debug("Looking for scheduling by timer handle: " + timerHandle);

		Query query = SchedulingEntity.createQueryWhereTimerHandle(
				this.entityManager, timerHandle);
		List<SchedulingEntity> result = query.getResultList();
		if (result.isEmpty()) {
			return null;
		}
		return result.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<SchedulingEntity> listSchedulings() {
		LOG.debug("Listing schedulings");

		Query query = SchedulingEntity.createQueryListAll(this.entityManager);
		List<SchedulingEntity> result = query.getResultList();
		return result;
	}

}
