/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.tasks.model.bean;

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
import net.link.safeonline.authentication.exception.InvalidCronExpressionException;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.tasks.dao.SchedulingDAO;
import net.link.safeonline.tasks.dao.TaskDAO;
import net.link.safeonline.tasks.dao.TaskHistoryDAO;
import net.link.safeonline.tasks.model.TaskScheduler;
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

	@Resource(name = "defaultCronExpression")
	private String defaultCronExpression = "0 * * * * ?";

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
		try {
			this.setTimer(scheduling);
		} catch (Exception ex) {
			LOG.debug("Exception while setting timer on: " + ex.getMessage());
		}

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
					this.defaultCronExpression);
			try {
				this.setTimer(defaultScheduling);
			} catch (Exception ex) {
			}
		}

		// check condition of current scheduling and tasks
		LOG.debug("Checking scheduling and task state");
		List<SchedulingEntity> schedulings = this.schedulingDAO
				.listSchedulings();
		for (SchedulingEntity scheduling : schedulings) {
			LOG.debug("checking scheduling: " + scheduling.getName());
			// check if the task still exists
			Collection<TaskEntity> tasks = scheduling.getTasks();
			for (TaskEntity taskEntity : tasks) {
				LOG.debug("persistent task found: " + taskEntity.getJndiName());
				try {
					EjbUtils.getEJB(taskEntity.getJndiName(), Task.class);
				} catch (Exception e) {
					LOG.debug("Removing task entity: "
							+ taskEntity.getJndiName());
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
				try {
					this.setTimer(scheduling);
				} catch (Exception ex) {
				}
			}
		}

		// find new tasks
		LOG.debug("Looking for new tasks");
		Map<String, Task> taskNameMap = EjbUtils.getComponentNames(
				Task.JNDI_PREFIX, Task.class);
		for (Entry<String, Task> taskEntry : taskNameMap.entrySet()) {
			String taskJndiName = Task.JNDI_PREFIX + "/" + taskEntry.getKey();
			String taskName = taskEntry.getValue().getName();
			TaskEntity taskEntity = this.taskDAO.findTaskEntity(taskJndiName);
			if (taskEntity == null) {
				LOG.debug("Found new task: " + taskJndiName);
				taskEntity = this.taskDAO.addTaskEntity(taskJndiName, taskName,
						defaultScheduling);
				defaultScheduling.addTaskEntity(taskEntity);
			}
		}

	}

	public void preStop() {
		// empty
	}

	public void setTimer(SchedulingEntity scheduling)
			throws InvalidCronExpressionException {
		CronTrigger cronTrigger = null;
		try {
			cronTrigger = new CronTrigger("name", "group", scheduling
					.getCronExpression());
		} catch (Exception e) {
			throw new InvalidCronExpressionException();
		}
		try {
			Date fireDate = cronTrigger.computeFirstFireTime(null);
			if (fireDate.equals(scheduling.getFireDate())) {
				cronTrigger.triggered(null);
				fireDate = cronTrigger.getNextFireTime();
			}
			LOG.debug("Setting timer at: " + fireDate);
			Timer timer = createTimer(fireDate, scheduling.getName());
			scheduling.setTimerHandle(timer.getHandle());
			scheduling.setFireDate(fireDate);
		} catch (Exception e) {
			throw new EJBException(e);
		}
	}

	/**
	 * Creates a timer. We use some workaround code here because of the
	 * following issues:
	 * 
	 * http://jira.jboss.com/jira/browse/JBAS-3379
	 * http://jira.jboss.com/jira/browse/JBAS-3380
	 * http://www.jboss.com/index.html?module=bb&op=viewtopic&p=3893673
	 * 
	 * @param fireDate
	 * @param schedulingName
	 */
	private Timer createTimer(Date fireDate, String schedulingName) {
		int tries = 10;
		Timer timer = null;
		do {
			tries--;
			try {
				timer = this.timerService.createTimer(fireDate, schedulingName);
			} catch (Exception e) {
				LOG.error("error creating timer: " + e.getMessage(), e);
				LOG.error("trying again...");
			}
		} while (null == timer && tries > 0);
		if (null == timer) {
			throw new EJBException("could not create an EJB timer");
		}
		return timer;
	}

	public int getPriority() {
		return Startable.PRIORITY_DONT_CARE;
	}
}
