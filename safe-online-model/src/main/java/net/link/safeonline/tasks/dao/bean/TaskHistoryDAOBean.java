/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.tasks.dao.bean;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.entity.tasks.TaskHistoryEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.tasks.dao.TaskHistoryDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class TaskHistoryDAOBean implements TaskHistoryDAO {

    private static final Log                 LOG = LogFactory.getLog(TaskHistoryDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                    entityManager;

    private TaskHistoryEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, TaskHistoryEntity.QueryInterface.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public TaskHistoryEntity addTaskHistoryEntity(TaskEntity task, String message, boolean result, Date startDate, Date endDate) {

        TaskHistoryEntity taskHistoryEntity = new TaskHistoryEntity(task, message, result, startDate, endDate);
        task.addTaskHistoryEntity(taskHistoryEntity);
        this.entityManager.persist(taskHistoryEntity);
        return taskHistoryEntity;
    }

    public List<TaskHistoryEntity> listTaskHistory(TaskEntity task) {

        TaskEntity attachedTask = this.entityManager.find(TaskEntity.class, task.getJndiName());
        List<TaskHistoryEntity> taskHistoryList = attachedTask.getTaskHistory();
        for (TaskHistoryEntity history : taskHistoryList) {
            history.getId();
        }
        return taskHistoryList;
    }

    public void clearTaskHistory(TaskEntity task) {

        LOG.debug("Clearing history for task entity: " + task.getName());
        this.queryObject.clearTaskHistory(task);
    }

    public void clearAllTasksHistory() {

        LOG.debug("Clearing history for all tasks");
        this.queryObject.clearAllTasksHistory();
    }

    public void clearAllTasksHistory(long ageInMillis) {

        LOG.debug("Clearing history older than " + ageInMillis + "for all tasks");
        Date ageLimit = new Date(System.currentTimeMillis() - ageInMillis);
        this.queryObject.clearAllTasksHistory(ageLimit);
    }
}
