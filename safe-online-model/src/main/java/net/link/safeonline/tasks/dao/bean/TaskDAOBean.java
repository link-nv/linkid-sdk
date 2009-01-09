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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.tasks.dao.TaskDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = TaskDAO.JNDI_BINDING)
public class TaskDAOBean implements TaskDAO {

    private static final Log          LOG = LogFactory.getLog(TaskDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager             entityManager;

    private TaskEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, TaskEntity.QueryInterface.class);
    }

    public TaskEntity addTaskEntity(String jndiName, String name, SchedulingEntity scheduling) {

        LOG.debug("Adding task entity: " + name);

        TaskEntity taskEntity = new TaskEntity(jndiName, name, scheduling);
        entityManager.persist(taskEntity);
        return taskEntity;
    }

    public TaskEntity findTaskEntity(String jndiName) {

        LOG.debug("find task entity: " + jndiName);
        TaskEntity result = queryObject.findTaskEntity(jndiName);
        return result;
    }

    public List<TaskEntity> listTaskEntities() {

        LOG.debug("Listing task entities");
        List<TaskEntity> result = queryObject.listTaskEntities();
        return result;
    }

    public void removeTaskEntity(TaskEntity taskEntity) {

        LOG.debug("Removing task entity: " + taskEntity.getName());
        entityManager.remove(taskEntity);
    }
}
