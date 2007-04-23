/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.Task;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.model.ConfigurationManager;

import static net.link.safeonline.model.bean.HistoryCleanerConfigurationProviderBean.configAgeInMillis;

@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = Task.JNDI_PREFIX + "/" + "HistoryCleanerTaskBean")
public class HistoryCleanerTaskBean implements Task {

	private static final String name = "Subject history cleaner";

	@EJB
	private HistoryDAO historyDAO;

	@EJB
	private ConfigurationManager configurationManager;

	public HistoryCleanerTaskBean() {
		// empty
	}

	public String getName() {
		return name;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void perform() throws Exception {
		ConfigItemEntity configItem = this.configurationManager
				.findConfigItem(configAgeInMillis);
		long ageInMillis = Integer.parseInt(configItem.getValue());

		this.historyDAO.clearAllHistory(ageInMillis);
	}

}
