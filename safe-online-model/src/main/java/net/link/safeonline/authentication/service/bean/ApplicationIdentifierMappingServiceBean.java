/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.ApplicationIdentifierMappingService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.ApplicationManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATION_SECURITY_DOMAIN)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class ApplicationIdentifierMappingServiceBean implements ApplicationIdentifierMappingService {

    private static final Log     LOG = LogFactory.getLog(ApplicationIdentifierMappingServiceBean.class);

    @EJB
    private ApplicationManager   applicationManager;

    @EJB
    private UserIdMappingService userIdMappingService;

    @EJB
    private SubjectIdentifierDAO subjectIdentifierDAO;


    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public String getApplicationUserId(String username) throws PermissionDeniedException, ApplicationNotFoundException,
            SubscriptionNotFoundException, SubjectNotFoundException {

        LOG.debug("getUserId: " + username);
        checkPermission();
        ApplicationEntity application = this.applicationManager.getCallerApplication();
        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(SafeOnlineConstants.LOGIN_IDENTIFIER_DOMAIN,
                username);
        if (null == subject)
            throw new SubjectNotFoundException();
        String userId = this.userIdMappingService.getApplicationUserId(application.getName(), subject.getUserId());
        LOG.debug("userId: " + userId);
        return userId;
    }

    private void checkPermission() throws PermissionDeniedException {

        ApplicationEntity application = this.applicationManager.getCallerApplication();
        boolean allowed = application.isIdentifierMappingAllowed();
        if (false == allowed)
            throw new PermissionDeniedException("application not allowed to use the identifier mapping service");
    }

    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public String findUserId(String applicationName, String applicationUserId) throws ApplicationNotFoundException {

        LOG.debug("getUserId: " + applicationName + ", " + applicationUserId);
        return this.userIdMappingService.findUserId(applicationName, applicationUserId);
    }
}
