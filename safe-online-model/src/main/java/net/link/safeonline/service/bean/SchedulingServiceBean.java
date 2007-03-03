/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.SchedulingDAO;
import net.link.safeonline.dao.TaskDAO;
import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.entity.TaskEntity;
import net.link.safeonline.service.SchedulingService;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class SchedulingServiceBean implements SchedulingService {

	private static final Log LOG = LogFactory
			.getLog(SchedulingServiceBean.class);

	@EJB
	private TaskDAO taskDAO;

	@EJB
	private SchedulingDAO schedulingDAO;

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<TaskEntity> getTaskList() {
		return this.taskDAO.listTaskEntities();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<TaskEntity> getTaskListForScheduling(SchedulingEntity scheduling) {
		SchedulingEntity attachedScheduling = this.schedulingDAO
				.findSchedulingByName(scheduling.getName());
		if (attachedScheduling == null) {
			return new ArrayList<TaskEntity>();
		}
		try {
			return (List<TaskEntity>) attachedScheduling.getTasks();
		} catch (RuntimeException e) {
			LOG.debug("getting tasks of scheduling "
					+ attachedScheduling.getName() + " failed!");
			throw (e);
		}
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<SchedulingEntity> getSchedulingList() {
		return this.schedulingDAO.listSchedulings();
	}
}
