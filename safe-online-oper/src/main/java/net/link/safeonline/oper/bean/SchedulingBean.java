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
@LocalBinding(jndiBinding = Scheduling.JNDI_BINDING)
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

    @EJB(mappedName = SchedulingService.JNDI_BINDING)
    private SchedulingService       schedulingService;

    @In(create = true)
    FacesMessages                   facesMessages;


    @Factory("schedulingList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void schedulingListFactory() {

        schedulingList = schedulingService.getSchedulingList();
    }

    @Factory("taskList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void taskListFactory() {

        if (selectedScheduling == null) {
            taskList = schedulingService.listTaskList();
        } else {
            taskList = selectedScheduling.getTasks();
        }
    }

    @Factory("taskHistoryList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void taskHistoryListFactory() {

        taskHistoryList = schedulingService.getTaskHistoryList(selectedTask);
    }

    @Factory("newScheduling")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void newSchedulingFactory() {

        newScheduling = new SchedulingEntity();
    }

    @Factory("selectSchedulingList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> selectSchedulingListFactory() {

        List<SelectItem> selectSchedulingList = new ArrayList<SelectItem>();
        schedulingListFactory();
        for (SchedulingEntity scheduling : schedulingList) {
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

        selectedTask = null;
        return "schedulinglistview";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String schedulingView() {

        if (selectedScheduling == null) {
            selectedScheduling = selectedTask.getScheduling();
        }
        taskList = selectedScheduling.getTasks();
        return "schedulingview";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String taskListView() {

        selectedScheduling = null;
        taskList = schedulingService.listTaskList();
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

        schedulingService.performTask(selectedTask);
        return "successperformtask";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String performScheduling() {

        schedulingService.performScheduling(selectedScheduling);
        return "successperformscheduling";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String clearTaskHistory() {

        schedulingService.clearTaskHistory(selectedTask);
        taskHistoryListFactory();
        return "successcleartaskhistory";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String clearAllTasksHistory() {

        schedulingService.clearAllTasksHistory();
        return "successclearalltaskhistory";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String saveScheduling() {

        try {
            schedulingService.saveScheduling(selectedScheduling);
        } catch (InvalidCronExpressionException e) {
            facesMessages.addToControlFromResourceBundle("cronExpression", FacesMessage.SEVERITY_ERROR, "errorCronExpressionInvalid");
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

        LOG.debug("adding scheduling: " + newScheduling.getName());
        try {
            schedulingService.addScheduling(newScheduling);
        } catch (InvalidCronExpressionException e) {
            facesMessages.addToControlFromResourceBundle("cronExpression", FacesMessage.SEVERITY_ERROR, "errorCronExpressionInvalid");
            return null;
        } catch (ExistingSchedulingException e) {
            facesMessages
                              .addToControlFromResourceBundle("cronExpression", FacesMessage.SEVERITY_ERROR, "errorSchedulingAlreadyExists");
            return null;
        }
        return "successaddscheduling";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String saveTask() {

        schedulingService.saveTask(selectedTask);
        return "successsavetask";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String taskEditView() {

        return "taskeditview";
    }

}
