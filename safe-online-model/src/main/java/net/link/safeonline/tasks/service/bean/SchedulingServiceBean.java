/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.tasks.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingSchedulingException;
import net.link.safeonline.authentication.exception.InvalidCronExpressionException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.entity.tasks.TaskHistoryEntity;
import net.link.safeonline.tasks.dao.SchedulingDAO;
import net.link.safeonline.tasks.dao.TaskDAO;
import net.link.safeonline.tasks.dao.TaskHistoryDAO;
import net.link.safeonline.tasks.model.TaskScheduler;
import net.link.safeonline.tasks.service.SchedulingService;
import net.link.safeonline.tasks.service.SchedulingServiceRemote;

import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = SchedulingService.JNDI_BINDING)
public class SchedulingServiceBean implements SchedulingService, SchedulingServiceRemote {

    @EJB
    private TaskDAO        taskDAO;

    @EJB
    private SchedulingDAO  schedulingDAO;

    @EJB
    private TaskHistoryDAO taskHistoryDAO;

    @EJB
    private TaskScheduler  taskScheduler;


    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<TaskEntity> listTaskList() {

        List<TaskEntity> taskList = this.taskDAO.listTaskEntities();
        return taskList;
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<SchedulingEntity> getSchedulingList() {

        return this.schedulingDAO.listSchedulings();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<TaskHistoryEntity> getTaskHistoryList(TaskEntity task) {

        return this.taskHistoryDAO.listTaskHistory(task);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void performTask(TaskEntity task) {

        TaskEntity attachedEntity = this.taskDAO.findTaskEntity(task.getJndiName());
        this.taskScheduler.performTask(attachedEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void performScheduling(SchedulingEntity scheduling) {

        SchedulingEntity attachedEntity = this.schedulingDAO.findSchedulingByName(scheduling.getName());
        this.taskScheduler.performScheduling(attachedEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void clearTaskHistory(TaskEntity task) {

        this.taskHistoryDAO.clearTaskHistory(task);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void clearAllTasksHistory() {

        this.taskHistoryDAO.clearAllTasksHistory();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void saveScheduling(SchedulingEntity scheduling) throws InvalidCronExpressionException {

        SchedulingEntity attachedScheduling = this.schedulingDAO.findSchedulingByName(scheduling.getName());
        attachedScheduling.setCronExpression(scheduling.getCronExpression());
        attachedScheduling.setName(scheduling.getName());
        this.taskScheduler.setTimer(scheduling);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addScheduling(SchedulingEntity scheduling) throws InvalidCronExpressionException, ExistingSchedulingException {

        SchedulingEntity existingScheduling = this.schedulingDAO.findSchedulingByName(scheduling.getName());
        if (null != existingScheduling)
            throw new ExistingSchedulingException();

        this.schedulingDAO.addScheduling(scheduling.getName(), scheduling.getCronExpression());
        this.taskScheduler.setTimer(scheduling);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void saveTask(TaskEntity task) {

        TaskEntity attachedTask = this.taskDAO.findTaskEntity(task.getJndiName());
        attachedTask.setScheduling(task.getScheduling());
    }
}
