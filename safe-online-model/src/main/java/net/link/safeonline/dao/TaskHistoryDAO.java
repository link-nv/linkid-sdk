/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.TaskEntity;
import net.link.safeonline.entity.TaskHistoryEntity;

@Local
public interface TaskHistoryDAO {

	TaskHistoryEntity addTaskHistoryEntity(TaskEntity task, String message,
			boolean result, Date startDate, Date endDate);

	List<TaskHistoryEntity> getTaskHistory(TaskEntity task);

	void clearTaskHistory(TaskEntity task);

	void clearAllTasksHistory();

}
