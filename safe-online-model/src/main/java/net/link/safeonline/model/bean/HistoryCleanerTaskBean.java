/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.Task;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.dao.HistoryDAO;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Configurable
@Interceptors(ConfigurationInterceptor.class)
@Local(Task.class)
@LocalBinding(jndiBinding = HistoryCleanerTaskBean.JNDI_BINDING)
public class HistoryCleanerTaskBean implements Task {

    public static final String  JNDI_BINDING      = Task.JNDI_PREFIX + "HistoryCleanerTaskBean/local";

    private static final String name              = "Subject history cleaner";

    @EJB
    private HistoryDAO          historyDAO;

    @Configurable(name = "History Age (ms)", group = "User history cleaner")
    private Integer             configAgeInMillis = 10 * 60 * 1000;


    public HistoryCleanerTaskBean() {

        // empty
    }

    public String getName() {

        return name;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform()
            throws Exception {

        long ageInMillis = this.configAgeInMillis;
        Date ageLimit = new Date(System.currentTimeMillis() - ageInMillis);
        this.historyDAO.clearAllHistory(ageLimit);
    }

}
