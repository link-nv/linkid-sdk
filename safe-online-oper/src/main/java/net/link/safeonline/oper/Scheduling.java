/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import javax.ejb.Local;

@Local
public interface Scheduling {

	void schedulingListFactory();

	void taskListFactory();

	void taskHistoryListFactory();

	void destroyCallback();

	String schedulingListView();

	String schedulingView();

	String taskListView();

	String taskView();

	String taskHistoryView();

	String performTask();

	String performScheduling();

	String clearTaskHistory();

	String clearAllTasksHistory();

}
