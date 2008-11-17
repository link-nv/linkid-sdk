/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.service.bean;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.security.DenyAll;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectMismatchException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ReAuthenticationService;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NonEmptyString;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@Stateful
@LocalBinding(jndiBinding = ReAuthenticationService.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class, InputValidation.class })
public class ReAuthenticationServiceBean implements ReAuthenticationService {

    // TODO: update for remote devices

    private final static Log  LOG = LogFactory.getLog(ReAuthenticationServiceBean.class);

    private SubjectEntity     authenticatedSubject;

    private Set<DeviceEntity> authenticationDevices;

    @EJB(mappedName = SubjectManager.JNDI_BINDING)
    private SubjectManager    subjectManager;


    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("PostConstruct");
    }

    @DenyAll
    public Set<DeviceEntity> getAuthenticatedDevices() {

        return this.authenticationDevices;
    }

    /**
     * Sets the re-authenticated subject. If already set checks if its the same.
     * 
     * @param subject
     * @throws SubjectMismatchException
     * @throws PermissionDeniedException
     */
    @DenyAll
    public void setAuthenticatedSubject(SubjectEntity subject)
            throws SubjectMismatchException, PermissionDeniedException {

        LOG.debug("set re-auth subject: " + subject.getUserId());
        SubjectEntity targetSubject = this.subjectManager.getCallerSubject();
        if (targetSubject.equals(subject))
            throw new PermissionDeniedException("target subject is equals source subject");
        if (null == this.authenticatedSubject) {
            this.authenticatedSubject = subject;
            return;
        }
        if (!this.authenticatedSubject.equals(subject))
            throw new SubjectMismatchException();
    }

    @DenyAll
    public boolean authenticate(@NonEmptyString String login, @NonEmptyString String password)
            throws SubjectNotFoundException, DeviceNotFoundException, SubjectMismatchException, PermissionDeniedException,
            DeviceDisabledException {

        /*
         * SubjectEntity subject = this.passwordDeviceService.authenticate(login, password); if (null == subject) return false;
         * LOG.debug("sucessfully authenticated " + login);
         */

        /*
         * Safe the state in this stateful session bean.
         */
        /*
         * setAuthenticatedSubject(subject); DeviceEntity passwordDevice =
         * this.deviceDAO.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID); addAuthenticationDevice(passwordDevice);
         */
        /*
         * Communicate that the authentication process can continue.
         */
        return true;
    }

    @DenyAll
    @Remove
    public void abort() {

        LOG.debug("abort");
        this.authenticatedSubject = null;
        this.authenticationDevices = null;
    }

}
