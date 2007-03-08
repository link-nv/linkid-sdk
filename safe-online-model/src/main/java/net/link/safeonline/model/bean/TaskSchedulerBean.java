/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.Startable;
import net.link.safeonline.Task;
import net.link.safeonline.dao.SchedulingDAO;
import net.link.safeonline.dao.TaskDAO;
import net.link.safeonline.dao.TaskHistoryDAO;
import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.entity.TaskEntity;
import net.link.safeonline.model.TaskScheduler;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.quartz.CronTrigger;

@Stateless
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "TaskSchedulerBean")
public class TaskSchedulerBean implements TaskScheduler {

	private static final Log LOG = LogFactory.getLog(TaskSchedulerBean.class);

	@EJB
	private TaskDAO taskDAO;

	@EJB
	private SchedulingDAO schedulingDAO;

	@EJB
	private TaskHistoryDAO taskHistoryDAO;

	@Resource
	private TimerService timerService;

	// private static String defaultCronExpression = "0 0 3 * * ?";
	private static String defaultCronExpression = "0 * * * * ?";

	@Timeout
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void timeOut(Timer timer) {
		String schedulingName = (String) timer.getInfo();
		SchedulingEntity scheduling = this.schedulingDAO
				.findSchedulingByName(schedulingName);

		// the scheduling does not exist anymore
		// we just return without setting this timer again
		if (scheduling == null) {
			LOG.debug("Could not find scheduling for timer: " + schedulingName);
			return;
		}

		// the scheduling apparantly has another timer still running
		// we just return without setting this timer again
		if (!scheduling.getTimerHandle().equals(timer.getHandle())) {
			LOG.debug("Ignoring duplicate timer for scheduling: "
					+ scheduling.getName());
			return;
		}

		// do the job
		try {
			this.performScheduling(scheduling);
		} catch (Exception e) {
			LOG.debug("Exception while performing scheduling: "
					+ e.getMessage());
		}

		// restore the timer
		this.setTimer(scheduling);

	}

	public void performTask(TaskEntity taskEntity) {
		Date startDate = null;
		Date endDate = null;
		try {
			Task task = EjbUtils.getEJB(taskEntity.getJndiName(), Task.class);
			LOG.debug("Firing task " + task.getName());
			startDate = new Date(System.currentTimeMillis());
			task.perform();
			endDate = new Date(System.currentTimeMillis());
		} catch (Exception e) {
			endDate = new Date(System.currentTimeMillis());
			LOG.debug("Could not get Task: " + taskEntity.getJndiName());
			this.taskHistoryDAO.addTaskHistoryEntity(taskEntity,
					e.getMessage(), false, startDate, endDate);
			return;
		}
		this.taskHistoryDAO.addTaskHistoryEntity(taskEntity, "", true,
				startDate, endDate);
	}

	public void performScheduling(SchedulingEntity scheduling) {
		Collection<TaskEntity> taskEntities = scheduling.getTasks();

		LOG.debug("Scheduling tasks in " + scheduling.getName());
		// perform the tasks
		for (TaskEntity taskEntity : taskEntities) {
			this.performTask(taskEntity);
		}
	}

	public void postStart() {

		// find or create the default scheduling
		SchedulingEntity defaultScheduling = this.schedulingDAO
				.findSchedulingByName("default");
		if (defaultScheduling == null) {
			defaultScheduling = this.schedulingDAO.addScheduling("default",
					defaultCronExpression);
			this.setTimer(defaultScheduling);
		}

		// check condition of current scheduling and tasks
		LOG.debug("Checking scheduling and task state");
		List<SchedulingEntity> schedulings = this.schedulingDAO
				.listSchedulings();
		for (SchedulingEntity scheduling : schedulings) {
			// check if the task still exists
			Collection<TaskEntity> tasks = scheduling.getTasks();
			for (TaskEntity taskEntity : tasks) {
				try {
					EjbUtils.getEJB(taskEntity.getJndiName(), Task.class);
				} catch (Exception e) {
					LOG.debug("Removing task entity: " + taskEntity.getName());
					this.taskDAO.removeTaskEntity(taskEntity);
				}
			}

			// check timer condition
			LOG.debug("Checking timers");
			TimerHandle timerHandle = scheduling.getTimerHandle();
			try {
				timerHandle.getTimer();
			} catch (Exception e) {
				LOG.debug("Resetting timer on " + scheduling.getName());
				this.setTimer(scheduling);
			}
		}

		// find new tasks
		LOG.debug("Looking for new tasks");
		Map<String, Task> taskNameMap = EjbUtils.getComponentNames(
				Task.JNDI_PREFIX, Task.class);
		for (Entry<String, Task> taskEntry : taskNameMap.entrySet()) {
			String taskJndiName = taskEntry.getKey();
			String taskName = taskEntry.getValue().getName();
			TaskEntity taskEntity = this.taskDAO.findTaskEntity(taskJndiName);
			if (taskEntity == null) {
				LOG.debug("Found new task: " + taskJndiName);
				taskEntity = this.taskDAO.addTaskEntity(Task.JNDI_PREFIX + "/"
						+ taskJndiName, taskName, defaultScheduling);
			}
		}

	}

	public void preStop() {
		// empty
	}

	public void setTimer(SchedulingEntity scheduling) {
		try {
			CronTrigger cronTrigger = new CronTrigger("name", "group",
					scheduling.getCronExpression());
			Date fireDate = cronTrigger.computeFirstFireTime(null);
			if (fireDate.equals(scheduling.getFireDate())) {
				cronTrigger.triggered(null);
				fireDate = cronTrigger.getNextFireTime();
			}
			LOG.debug("Setting timer at: " + fireDate);
			Timer timer = timerService.createTimer(fireDate, scheduling
					.getName());
			scheduling.setTimerHandle(timer.getHandle());
			scheduling.setFireDate(fireDate);
		} catch (Exception e) {
			throw new EJBException(e);
		}
	}

	public int getPriority() {
		return Startable.PRIORITY_DONT_CARE;
	}
}
