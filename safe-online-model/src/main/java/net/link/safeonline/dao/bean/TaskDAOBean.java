/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.TaskDAO;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class TaskDAOBean implements TaskDAO {

	private static final Log LOG = LogFactory.getLog(TaskDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public TaskEntity addTaskEntity(String jndiName, String name,
			SchedulingEntity scheduling) {
		LOG.debug("Adding task entity: " + name);

		TaskEntity taskEntity = new TaskEntity(jndiName, name, scheduling);
		this.entityManager.persist(taskEntity);
		return taskEntity;
	}

	@SuppressWarnings("unchecked")
	public TaskEntity findTaskEntity(String jndiName) {
		LOG.debug("find task entity: " + jndiName);

		Query query = TaskEntity.createQueryWhereJndiName(this.entityManager,
				jndiName);
		List<TaskEntity> result = query.getResultList();
		if (result.isEmpty()) {
			return null;
		}
		return result.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<TaskEntity> listTaskEntities() {
		LOG.debug("Listing task entities");

		Query query = TaskEntity.createQueryListAll(this.entityManager);
		List<TaskEntity> result = query.getResultList();
		return result;
	}

	public void removeTaskEntity(TaskEntity taskEntity) {
		LOG.debug("Removing task entity: " + taskEntity.getName());

		this.entityManager.remove(taskEntity);
	}
}
