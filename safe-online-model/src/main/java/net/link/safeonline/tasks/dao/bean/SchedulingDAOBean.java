/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.tasks.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TimerHandle;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.tasks.dao.SchedulingDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class SchedulingDAOBean implements SchedulingDAO {

	private static final Log LOG = LogFactory.getLog(SchedulingDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private SchedulingEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, SchedulingEntity.QueryInterface.class);
	}

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

	public SchedulingEntity findSchedulingByName(String name) {
		LOG.debug("Looking for scheduling by name: " + name);
		SchedulingEntity result = this.queryObject.findSchedulingByName(name);
		return result;
	}

	public SchedulingEntity findSchedulingByTimerHandle(TimerHandle timerHandle) {
		LOG.debug("Looking for scheduling by timer handle: " + timerHandle);
		SchedulingEntity result = this.queryObject
				.findSchedulingByTimerHandle(timerHandle);
		return result;
	}

	public List<SchedulingEntity> listSchedulings() {
		LOG.debug("Listing schedulings");
		List<SchedulingEntity> result = this.queryObject.listSchedulings();
		return result;
	}

}
