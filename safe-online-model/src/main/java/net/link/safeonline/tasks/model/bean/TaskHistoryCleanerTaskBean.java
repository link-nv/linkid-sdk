/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.tasks.model.bean;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.Task;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.tasks.dao.TaskHistoryDAO;

import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = Task.JNDI_PREFIX + "/"
		+ "TaskHistoryCleanerTaskBean")
@Interceptors(ConfigurationInterceptor.class)
@Configurable
public class TaskHistoryCleanerTaskBean implements Task {

	private static final String name = "Task history cleaner";

	@EJB
	private TaskHistoryDAO taskHistoryDAO;

	@Configurable(group = "Task history cleaner", name = "Task history age limit (ms)")
	private String configAgeInMillis = "600000";

	public TaskHistoryCleanerTaskBean() {
		// empty
	}

	public String getName() {
		return name;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void perform() throws Exception {
		long ageInMillis = Integer.parseInt(configAgeInMillis);

		this.taskHistoryDAO.clearAllTasksHistory(ageInMillis);
	}

}
