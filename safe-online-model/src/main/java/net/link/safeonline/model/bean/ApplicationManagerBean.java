/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.model.ApplicationManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATION_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = ApplicationManager.JNDI_BINDING)
public class ApplicationManagerBean implements ApplicationManager {

    private static final Log LOG = LogFactory.getLog(ApplicationManagerBean.class);

    @Resource
    private SessionContext   context;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO   applicationDAO;


    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public ApplicationEntity getCallerApplication() {

        Principal callerPrincipal = this.context.getCallerPrincipal();
        String applicationName = callerPrincipal.getName();
        LOG.debug("get caller application: " + applicationName);
        ApplicationEntity callerApplication;
        try {
            callerApplication = this.applicationDAO.getApplication(applicationName);
        } catch (ApplicationNotFoundException e) {
            throw new EJBException("application not found: " + e.getMessage(), e);
        }
        return callerApplication;
    }
}
