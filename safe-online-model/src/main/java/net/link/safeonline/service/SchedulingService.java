/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.entity.TaskEntity;

@Local
@Remote
public interface SchedulingService {

	List<TaskEntity> getTaskList();

	List<TaskEntity> getTaskListForScheduling(SchedulingEntity scheduling);

	List<SchedulingEntity> getSchedulingList();

}
