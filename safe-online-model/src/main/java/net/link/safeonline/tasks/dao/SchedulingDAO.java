/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.tasks.dao;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.TimerHandle;

import net.link.safeonline.entity.tasks.SchedulingEntity;

@Local
public interface SchedulingDAO {

	SchedulingEntity findSchedulingByTimerHandle(TimerHandle timerHandle);

	SchedulingEntity findSchedulingByName(String name);

	List<SchedulingEntity> listSchedulings();

	SchedulingEntity addScheduling(String name, String cronExpression);

	void removeScheduling(String name);

}
