/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;
import javax.ejb.Timer;

import net.link.safeonline.Startable;
import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.entity.TaskEntity;

@Local
public interface TaskScheduler extends Startable {

	/**
	 * Performs the task related to the timer
	 * 
	 * @param timer
	 */
	void timeOut(Timer timer);

	void performTask(TaskEntity task);

	void performScheduling(SchedulingEntity scheduling);

}
