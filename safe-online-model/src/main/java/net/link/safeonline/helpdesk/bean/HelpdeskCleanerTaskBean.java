/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.Task;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.helpdesk.dao.HelpdeskContextDAO;
import net.link.safeonline.helpdesk.dao.HelpdeskEventDAO;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = HelpdeskCleanerTaskBean.JNDI_BINDING)
@Interceptors(ConfigurationInterceptor.class)
@Configurable
public class HelpdeskCleanerTaskBean implements Task {

    private static final String name                    = "Helpdesk event history cleaner";

    public static final String JNDI_BINDING = Task.JNDI_PREFIX + "/HelpdeskCleanerTaskBean/local";

    @EJB
    private HelpdeskEventDAO    helpdeskEventDAO;

    @EJB
    private HelpdeskContextDAO  helpdeskContextDAO;

    @Configurable(name = "Info Event Age (min)", group = "Helpdesk event cleaner")
    private Integer             configInfoAgeInMinutes  = 24 * 60;

    @Configurable(name = "Error Event Age (min)", group = "Helpdesk event cleaner")
    private Integer             configErrorAgeInMinutes = 5 * 24 * 60;


    public HelpdeskCleanerTaskBean() {

        // empty
    }

    public String getName() {

        return name;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform() throws Exception {

        long infoAgeInMinutes = this.configInfoAgeInMinutes;
        long errorAgeInMinutes = this.configErrorAgeInMinutes;

        this.helpdeskEventDAO.clearEvents(infoAgeInMinutes, LogLevelType.INFO);
        this.helpdeskEventDAO.clearEvents(errorAgeInMinutes, LogLevelType.ERROR);

        // cleanup contexts with no events attached to it
        this.helpdeskContextDAO.cleanup();
    }

}
