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

@Local
public interface Scheduling {

	void schedulingListFactory();

	void taskListFactory();

	void taskHistoryListFactory();

	void newSchedulingFactory();

	void destroyCallback();

	List<SelectItem> selectSchedulingListFactory();

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
