/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.tasks.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;


@Local
public interface TaskDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/TaskDAOBean/local";


    TaskEntity findTaskEntity(String jndiName);

    List<TaskEntity> listTaskEntities();

    TaskEntity addTaskEntity(String jndiName, String name, SchedulingEntity scheduling);

    void removeTaskEntity(TaskEntity taskEntity);

}
