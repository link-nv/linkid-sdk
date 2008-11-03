/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;


import net.link.safeonline.SafeOnlineService;

@Local
public interface Scheduling extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/SchedulingBean/local";

    /*
     * Factories.
     */
    void schedulingListFactory();

    void taskListFactory();

    void taskHistoryListFactory();

    void newSchedulingFactory();

    List<SelectItem> selectSchedulingListFactory();

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Actions.
     */
    String schedulingListView();

    String schedulingView();

    String taskListView();

    String taskView();

    String taskHistoryView();

    String performTask();

    String performScheduling();

    String clearTaskHistory();

    String clearAllTasksHistory();

    String saveScheduling();

    String saveTask();

    String editSchedulingView();

    String addSchedulingView();

    String addScheduling();

    String taskEditView();

}
