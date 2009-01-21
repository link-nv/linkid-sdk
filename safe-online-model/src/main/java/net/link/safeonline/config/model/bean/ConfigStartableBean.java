/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model.bean;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.audit.bean.AuditCleanerTaskBean;
import net.link.safeonline.audit.bean.AuditSyslogBean;
import net.link.safeonline.authentication.service.bean.SamlAuthorityServiceBean;
import net.link.safeonline.authentication.service.bean.WSAuthenticationServiceBean;
import net.link.safeonline.helpdesk.bean.HelpdeskCleanerTaskBean;
import net.link.safeonline.helpdesk.bean.HelpdeskManagerBean;
import net.link.safeonline.messaging.bean.EmailBean;
import net.link.safeonline.model.bean.ClockDriftDetectorTaskBean;
import net.link.safeonline.model.bean.HelpdeskContactBean;
import net.link.safeonline.model.bean.HistoryCleanerTaskBean;
import net.link.safeonline.model.bean.IdGeneratorBean;
import net.link.safeonline.model.bean.UsageStatisticTaskBean;
import net.link.safeonline.model.bean.WSSecurityConfigurationBean;
import net.link.safeonline.notification.service.bean.NotificationMessageQueueTaskBean;
import net.link.safeonline.tasks.model.bean.TaskHistoryCleanerTaskBean;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = ConfigStartableBean.JNDI_BINDING)
public class ConfigStartableBean extends AbstractConfigStartableBean {

    public static final String JNDI_BINDING = Startable.JNDI_PREFIX + "ConfigStartableBean";


    public ConfigStartableBean() {

        configurationBeans = new Class[] { AuditCleanerTaskBean.class, AuditSyslogBean.class, SamlAuthorityServiceBean.class,
                WSSecurityConfigurationBean.class, HelpdeskCleanerTaskBean.class, HelpdeskManagerBean.class, EmailBean.class,
                ClockDriftDetectorTaskBean.class, HelpdeskContactBean.class, HistoryCleanerTaskBean.class, IdGeneratorBean.class,
                UsageStatisticTaskBean.class, NotificationMessageQueueTaskBean.class, TaskHistoryCleanerTaskBean.class,
                WSAuthenticationServiceBean.class };

    }

    @Override
    public int getPriority() {

        return Startable.PRIORITY_BOOTSTRAP;
    }
}
