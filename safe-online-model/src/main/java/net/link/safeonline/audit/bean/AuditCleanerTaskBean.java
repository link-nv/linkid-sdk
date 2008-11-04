/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.Task;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * Task for cleaning up audit contexts.
 * 
 * @author wvdhaute
 * 
 */
@Stateless
@LocalBinding(jndiBinding = AuditCleanerTaskBean.JNDI_BINDING)
@Interceptors(ConfigurationInterceptor.class)
@Configurable
public class AuditCleanerTaskBean implements Task {

    public static final String JNDI_BINDING = Task.JNDI_PREFIX + "/AuditCleanerTaskBean/local";

    private static final String name               = "Audit cleaner";

    @EJB
    private AuditContextDAO     auditContextDAO;

    @Configurable(name = "Audit Record Age (min)", group = "Audit cleaner")
    private Integer             configAgeInMinutes = 7 * 24 * 60;


    public String getName() {

        return name;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform() throws Exception {

        this.auditContextDAO.cleanup(this.configAgeInMinutes);
    }

}
