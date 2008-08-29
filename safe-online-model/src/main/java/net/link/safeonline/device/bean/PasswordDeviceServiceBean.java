/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.bean;

import java.util.Collections;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.device.PasswordDeviceServiceRemote;
import net.link.safeonline.device.backend.PasswordManager;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class PasswordDeviceServiceBean implements PasswordDeviceService, PasswordDeviceServiceRemote {

    private final static Log    LOG = LogFactory.getLog(PasswordDeviceServiceBean.class);

    @EJB
    private SubjectService      subjectService;

    @EJB
    private PasswordManager     passwordManager;

    @EJB
    private SecurityAuditLogger securityAuditLogger;

    @EJB
    private HistoryDAO          historyDAO;


    public SubjectEntity authenticate(String loginName, String password) throws DeviceNotFoundException,
            SubjectNotFoundException {

        LOG.debug("authenticate \"" + loginName + "\"");

        SubjectEntity subject = this.subjectService.getSubjectFromUserName(loginName);

        boolean validationResult = false;
        try {
            validationResult = this.passwordManager.validatePassword(subject, password);
        } catch (DeviceNotFoundException e) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(),
                    "password device not found");
            throw e;
        }

        if (!validationResult) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(),
                    "incorrect password");
            return null;
        }
        return subject;
    }

    public void register(String userId, String password) throws SubjectNotFoundException, DeviceNotFoundException {

        SubjectEntity subject = this.subjectService.getSubject(userId);
        register(subject, password);
    }

    public void register(SubjectEntity subject, String password) throws SubjectNotFoundException,
            DeviceNotFoundException {

        LOG.debug("register \"" + subject.getUserId() + "\"");
        try {
            this.passwordManager.setPassword(subject, password);
        } catch (PermissionDeniedException e) {
            throw new EJBException("Not allowed to set password");
        }

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID));

    }

    public void remove(SubjectEntity subject, String password) throws DeviceNotFoundException,
            PermissionDeniedException, SubjectNotFoundException {

        LOG.debug("remove " + subject.getUserId());

        this.passwordManager.removePassword(subject, password);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REMOVAL, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID));

    }

    public void update(SubjectEntity subject, String oldPassword, String newPassword) throws PermissionDeniedException,
            DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("update \"" + subject.getUserId() + "\"");

        this.passwordManager.changePassword(subject, oldPassword, newPassword);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_UPDATE, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID));

    }

    public boolean isPasswordConfigured(SubjectEntity subject) throws SubjectNotFoundException, DeviceNotFoundException {

        return this.passwordManager.isPasswordConfigured(subject);
    }
}
