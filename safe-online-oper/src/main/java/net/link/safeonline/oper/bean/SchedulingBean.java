/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.entity.TaskEntity;
import net.link.safeonline.entity.TaskHistoryEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.Scheduling;
import net.link.safeonline.service.SchedulingService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("scheduling")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "SchedulingBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class SchedulingBean implements Scheduling {

	@DataModel("schedulingList")
	@SuppressWarnings("unused")
	private List<SchedulingEntity> schedulingList;

	@DataModelSelection("schedulingList")
	@Out(value = "selectedScheduling", required = false, scope = ScopeType.SESSION)
	@In(value = "selectedScheduling", required = false)
	@SuppressWarnings("unused")
	private SchedulingEntity selectedScheduling;

	@DataModelSelection("taskList")
	@Out(value = "selectedTask", required = false, scope = ScopeType.SESSION)
	@In(value = "selectedTask", required = false)
	@SuppressWarnings("unused")
	private TaskEntity selectedTask;

	@DataModel("taskList")
	@SuppressWarnings("unused")
	private List<TaskEntity> taskList;

	@DataModel("taskHistoryList")
	@SuppressWarnings("unused")
	private List<TaskHistoryEntity> taskHistoryList;

	@EJB
	private SchedulingService schedulingService;

	@In(create = true)
	FacesMessages facesMessages;

	@Factory("schedulingList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void schedulingListFactory() {
		this.schedulingList = this.schedulingService.getSchedulingList();
	}

	@Factory("taskList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void taskListFactory() {
		if (selectedScheduling == null) {
			this.taskList = this.schedulingService.getTaskList();
		} else {
			this.taskList = this.selectedScheduling.getTasks();
		}
	}

	@Factory("taskHistoryList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void taskHistoryListFactory() {
		this.taskHistoryList = this.schedulingService
				.getTaskHistoryList(this.selectedTask);
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		// empty
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String schedulingListView() {
		this.selectedTask = null;
		return "schedulinglistview";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String schedulingView() {
		if (this.selectedScheduling == null) {
			this.selectedScheduling = this.selectedTask.getScheduling();
		}
		return "schedulingview";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String taskListView() {
		this.selectedScheduling = null;
		return "tasklistview";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String taskView() {
		return "taskview";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String taskHistoryView() {
		return "taskhistoryview";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String performTask() {
		this.schedulingService.performTask(selectedTask);
		return this.taskHistoryView();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String performScheduling() {
		this.schedulingService.performScheduling(selectedScheduling);
		return this.schedulingView();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String clearTaskHistory() {
		this.schedulingService.clearTaskHistory(selectedTask);
		return this.taskView();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String clearAllTasksHistory() {
		this.schedulingService.clearAllTasksHistory();
		return this.taskView();
	}
}
