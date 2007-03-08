/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.TaskHistoryDAO;
import net.link.safeonline.entity.TaskEntity;
import net.link.safeonline.entity.TaskHistoryEntity;

@Stateless
public class TaskHistoryDAOBean implements TaskHistoryDAO {

	private static final Log LOG = LogFactory.getLog(TaskHistoryDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public TaskHistoryEntity addTaskHistoryEntity(TaskEntity task,
			String message, boolean result, Date startDate, Date endDate) {
		TaskHistoryEntity taskHistoryEntity = new TaskHistoryEntity(task,
				message, result, startDate, endDate);
		task.addTaskHistoryEntity(taskHistoryEntity);
		entityManager.persist(taskHistoryEntity);
		return taskHistoryEntity;
	}

	public List<TaskHistoryEntity> getTaskHistory(TaskEntity task) {
		TaskEntity attachedTask = this.entityManager.find(TaskEntity.class,
				task.getJndiName());
		List<TaskHistoryEntity> taskHistoryList = attachedTask.getTaskHistory();
		for (TaskHistoryEntity history : taskHistoryList) {
			history.getId();
		}
		return taskHistoryList;
	}

	public void clearTaskHistory(TaskEntity task) {
		LOG.debug("Clearing history for task entity: " + task.getName());

		Query query = TaskHistoryEntity.createQueryDeleteWhereTask(
				this.entityManager, task);
		query.executeUpdate();
	}

	public void clearAllTasksHistory() {
		LOG.debug("Clearing history for all tasks");

		Query query = TaskHistoryEntity.createQueryDelete(this.entityManager);
		query.executeUpdate();
	}

	public void clearAllTasksHistory(long ageInMillis) {
		LOG.debug("Clearing history older than " + ageInMillis
				+ "for all tasks");

		Query query = TaskHistoryEntity.createQueryDeleteWhereOlder(
				this.entityManager, ageInMillis);
		query.executeUpdate();
	}
}
