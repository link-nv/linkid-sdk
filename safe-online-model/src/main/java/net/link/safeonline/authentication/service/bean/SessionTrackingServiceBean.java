/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.service.ApplicationIdentifierMappingService;
import net.link.safeonline.authentication.service.SessionTrackingService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.model.ApplicationManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


/**
 * <h2>{@link SessionTrackingServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Apr 2, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATION_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = SessionTrackingService.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class SessionTrackingServiceBean implements SessionTrackingService {

    private static final Log            LOG = LogFactory.getLog(SessionTrackingServiceBean.class);

    @EJB(mappedName = ApplicationManager.JNDI_BINDING)
    ApplicationManager                  applicationManager;

    @EJB(mappedName = ApplicationIdentifierMappingService.JNDI_BINDING)
    ApplicationIdentifierMappingService applicationIdentifierMappingService;


    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public void getAssertions(String session, String applicationUserId, List<String> applicationPools) {

        ApplicationEntity application = applicationManager.getCallerApplication();

        String userId = applicationIdentifierMappingService.findUserId(application, applicationUserId);

        LOG.debug("get assertions");

    }

}
