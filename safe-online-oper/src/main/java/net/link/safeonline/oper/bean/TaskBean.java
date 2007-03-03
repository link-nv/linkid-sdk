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

import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.entity.TaskEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.Task;
import net.link.safeonline.service.SchedulingService;

@Stateful
@Name("task")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "TaskBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class TaskBean implements Task {

	@DataModel
	@SuppressWarnings("unused")
	private List<TaskEntity> taskList;

	@DataModelSelection("taskList")
	@Out(value = "selectedTask", required = false, scope = ScopeType.SESSION)
	private TaskEntity selectedTask;

	@Out(value = "selectedScheduling", required = false, scope = ScopeType.SESSION)
	@SuppressWarnings("unused")
	private SchedulingEntity selectedScheduling;

	@EJB
	private SchedulingService schedulingService;

	@In(create = true)
	FacesMessages facesMessages;

	@Factory("taskList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void taskListFactory() {
		this.taskList = this.schedulingService.getTaskList();
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		// empty
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String view() {
		return "taskview";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String viewSchedulingForTask() {
		this.selectedScheduling = this.selectedTask.getScheduling();
		return "schedulingview";
	}
}
