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
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.Scheduling;
import net.link.safeonline.service.SchedulingService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static final Log LOG = LogFactory.getLog(SchedulingBean.class);

	@DataModel("schedulingList")
	@SuppressWarnings("unused")
	private List<SchedulingEntity> schedulingList;

	@DataModelSelection("schedulingList")
	@Out(value = "selectedScheduling", required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private SchedulingEntity selectedScheduling;

	@DataModel("taskListSelectedScheduling")
	@SuppressWarnings("unused")
	private List<TaskEntity> taskListSelectedScheduling;

	@DataModelSelection("taskListSelectedScheduling")
	@Out(value = "selectedTask", required = false, scope = ScopeType.SESSION)
	@In(value = "selectedTask", required = false)
	@SuppressWarnings("unused")
	private TaskEntity selectedTask;

	@EJB
	private SchedulingService schedulingService;

	@In(create = true)
	FacesMessages facesMessages;

	@Factory("schedulingList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void schedulingListFactory() {
		this.schedulingList = this.schedulingService.getSchedulingList();
	}

	@Factory("taskListSelectedScheduling")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void taskListSelectedSchedulingFactory() {
		LOG.debug("taskListSelectedSchedulingFactory");
		this.taskListSelectedScheduling = this.selectedScheduling.getTasks();
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		// empty
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String view() {
		return "schedulingview";
	}
}
