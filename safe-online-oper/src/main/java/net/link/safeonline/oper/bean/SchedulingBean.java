/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.exception.ExistingSchedulingException;
import net.link.safeonline.authentication.exception.InvalidCronExpressionException;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.entity.tasks.TaskHistoryEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.Scheduling;
import net.link.safeonline.tasks.service.SchedulingService;

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
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("scheduling")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "SchedulingBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class SchedulingBean implements Scheduling {

    private static final Log        LOG = LogFactory.getLog(SchedulingBean.class);

    @DataModel("schedulingList")
    private List<SchedulingEntity>  schedulingList;

    @DataModelSelection("schedulingList")
    @Out(value = "selectedScheduling", required = false, scope = ScopeType.SESSION)
    @In(value = "selectedScheduling", required = false)
    private SchedulingEntity        selectedScheduling;

    @DataModelSelection("taskList")
    @Out(value = "selectedTask", required = false, scope = ScopeType.SESSION)
    @In(value = "selectedTask", required = false)
    private TaskEntity              selectedTask;

    @DataModel("taskList")
    @SuppressWarnings("unused")
    private List<TaskEntity>        taskList;

    @DataModel("taskHistoryList")
    @SuppressWarnings("unused")
    private List<TaskHistoryEntity> taskHistoryList;

    @Out(value = "newScheduling", required = false)
    @In(value = "newScheduling", required = false)
    private SchedulingEntity        newScheduling;

    @EJB
    private SchedulingService       schedulingService;

    @In(create = true)
    FacesMessages                   facesMessages;


    @Factory("schedulingList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void schedulingListFactory() {

        this.schedulingList = this.schedulingService.getSchedulingList();
    }

    @Factory("taskList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void taskListFactory() {

        if (this.selectedScheduling == null) {
            this.taskList = this.schedulingService.listTaskList();
        } else {
            this.taskList = this.selectedScheduling.getTasks();
        }
    }

    @Factory("taskHistoryList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void taskHistoryListFactory() {

        this.taskHistoryList = this.schedulingService.getTaskHistoryList(this.selectedTask);
    }

    @Factory("newScheduling")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void newSchedulingFactory() {

        this.newScheduling = new SchedulingEntity();
    }

    @Factory("selectSchedulingList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> selectSchedulingListFactory() {

        List<SelectItem> selectSchedulingList = new ArrayList<SelectItem>();
        this.schedulingListFactory();
        for (SchedulingEntity scheduling : this.schedulingList) {
            SelectItem selectItem = new SelectItem(scheduling.getName());
            selectSchedulingList.add(selectItem);
        }
        return selectSchedulingList;
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
        this.taskList = this.selectedScheduling.getTasks();
        return "schedulingview";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String taskListView() {

        this.selectedScheduling = null;
        this.taskList = this.schedulingService.listTaskList();
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

        this.schedulingService.performTask(this.selectedTask);
        return "successperformtask";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String performScheduling() {

        this.schedulingService.performScheduling(this.selectedScheduling);
        return "successperformscheduling";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String clearTaskHistory() {

        this.schedulingService.clearTaskHistory(this.selectedTask);
        taskHistoryListFactory();
        return "successcleartaskhistory";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String clearAllTasksHistory() {

        this.schedulingService.clearAllTasksHistory();
        return "successclearalltaskhistory";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String saveScheduling() {

        try {
            this.schedulingService.saveScheduling(this.selectedScheduling);
        } catch (InvalidCronExpressionException e) {
            this.facesMessages.addToControlFromResourceBundle("cronExpression", FacesMessage.SEVERITY_ERROR, "errorCronExpressionInvalid");
            return null;
        }
        return "successsavescheduling";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String editSchedulingView() {

        return "schedulingedit";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String addSchedulingView() {

        return "addschedulingview";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String addScheduling() {

        LOG.debug("adding scheduling: " + this.newScheduling.getName());
        try {
            this.schedulingService.addScheduling(this.newScheduling);
        } catch (InvalidCronExpressionException e) {
            this.facesMessages.addToControlFromResourceBundle("cronExpression", FacesMessage.SEVERITY_ERROR, "errorCronExpressionInvalid");
            return null;
        } catch (ExistingSchedulingException e) {
            this.facesMessages
                              .addToControlFromResourceBundle("cronExpression", FacesMessage.SEVERITY_ERROR, "errorSchedulingAlreadyExists");
            return null;
        }
        return "successaddscheduling";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String saveTask() {

        this.schedulingService.saveTask(this.selectedTask);
        return "successsavetask";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String taskEditView() {

        return "taskeditview";
    }

}
