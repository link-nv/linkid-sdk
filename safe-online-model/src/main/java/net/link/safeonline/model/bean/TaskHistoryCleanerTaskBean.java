/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import static net.link.safeonline.model.bean.TaskHistoryCleanerConfigurationProviderBean.configAgeInMillis;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.Task;
import net.link.safeonline.dao.TaskHistoryDAO;
import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.model.ConfigurationManager;

import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = Task.JNDI_PREFIX + "/"
		+ "TaskHistoryCleanerTaskBean")
public class TaskHistoryCleanerTaskBean implements Task {

	private static final String name = "Task history cleaner";

	@EJB
	private TaskHistoryDAO taskHistoryDAO;

	@EJB
	private ConfigurationManager configurationManager;

	public TaskHistoryCleanerTaskBean() {
		// empty
	}

	public String getName() {
		return name;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void perform() {
		ConfigItemEntity configItem = this.configurationManager
				.getConfigItem(configAgeInMillis);
		long ageInMillis = Integer.parseInt(configItem.getValue());

		this.taskHistoryDAO.clearAllTasksHistory(ageInMillis);
	}

}
