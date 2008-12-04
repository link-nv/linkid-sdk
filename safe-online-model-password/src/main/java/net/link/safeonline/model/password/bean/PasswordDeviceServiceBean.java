/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.password.bean;

import java.util.Collections;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.model.password.PasswordDeviceServiceRemote;
import net.link.safeonline.model.password.PasswordManager;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


@Stateless
@LocalBinding(jndiBinding = PasswordDeviceService.JNDI_BINDING)
@RemoteBinding(jndiBinding = PasswordDeviceServiceRemote.JNDI_BINDING)
public class PasswordDeviceServiceBean implements PasswordDeviceService, PasswordDeviceServiceRemote {

    private final static Log    LOG = LogFactory.getLog(PasswordDeviceServiceBean.class);

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService      subjectService;

    @EJB(mappedName = PasswordManager.JNDI_BINDING)
    private PasswordManager     passwordManager;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger securityAuditLogger;

    @EJB(mappedName = HistoryDAO.JNDI_BINDING)
    private HistoryDAO          historyDAO;


    public String authenticate(String userId, String password)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceDisabledException {

        LOG.debug("authenticate \"" + userId + "\"");

        SubjectEntity subject = this.subjectService.getSubject(userId);

        if (this.passwordManager.isDisabled(subject))
            throw new DeviceDisabledException();

        boolean validationResult = false;
        try {
            validationResult = this.passwordManager.validatePassword(subject, password);
        } catch (DeviceNotFoundException e) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "password device not found");
            throw e;
        }

        if (!validationResult) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect password");
            return null;
        }
        return subject.getUserId();
    }

    public void register(String userId, String password)
            throws SubjectNotFoundException, DeviceNotFoundException {

        LOG.debug("register password for \"" + userId + "\"");
        SubjectEntity subject = this.subjectService.getSubject(userId);
        try {
            this.passwordManager.setPassword(subject, password);
        } catch (PermissionDeniedException e) {
            throw new EJBException("Not allowed to set password");
        }

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID));

    }

    public void remove(String userId)
            throws DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("remove password for " + userId);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        this.passwordManager.removePassword(subject);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REMOVAL, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID));

    }

    public void update(String userId, String oldPassword, String newPassword)
            throws PermissionDeniedException, DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("update password for \"" + userId + "\"");
        SubjectEntity subject = this.subjectService.getSubject(userId);

        this.passwordManager.changePassword(subject, oldPassword, newPassword);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_UPDATE, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID));

    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId)
            throws DeviceNotFoundException, SubjectNotFoundException {

        SubjectEntity subject = this.subjectService.getSubject(userId);

        LOG.debug("disable password for \"" + subject.getUserId() + "\"");
        this.passwordManager.disablePassword(subject, true);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_DISABLE, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID));
    }

    /**
     * {@inheritDoc}
     */
    public void enable(String userId, String password)
            throws DeviceNotFoundException, SubjectNotFoundException, PermissionDeniedException {

        SubjectEntity subject = this.subjectService.getSubject(userId);

        if (!this.passwordManager.validatePassword(subject, password))
            throw new PermissionDeniedException("Invalid password");

        LOG.debug("enable password for \"" + subject.getUserId() + "\"");
        this.passwordManager.disablePassword(subject, false);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_ENABLE, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID));
    }

    public boolean isPasswordConfigured(String userId)
            throws SubjectNotFoundException {

        SubjectEntity subject = this.subjectService.getSubject(userId);

        return this.passwordManager.isPasswordConfigured(subject);
    }
}
