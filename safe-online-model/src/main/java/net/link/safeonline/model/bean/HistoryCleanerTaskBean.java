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
import javax.interceptor.Interceptors;

import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.Configurable;
import net.link.safeonline.Task;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.model.ConfigurationInterceptor;

@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = Task.JNDI_PREFIX + "/" + "HistoryCleanerTaskBean")
@Interceptors(ConfigurationInterceptor.class)
@Configurable
public class HistoryCleanerTaskBean implements Task {

	private static final String name = "Subject history cleaner";

	@EJB
	private HistoryDAO historyDAO;

	@Configurable(name = "History Age (ms)", group = "User history cleaner")
	private String configAgeInMillis = "600000";

	public HistoryCleanerTaskBean() {
		// empty
	}

	public String getName() {
		return name;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void perform() throws Exception {
		long ageInMillis = Integer.parseInt(configAgeInMillis);

		this.historyDAO.clearAllHistory(ageInMillis);
	}

}
