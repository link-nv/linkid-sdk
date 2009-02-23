/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.password.bean;

import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.AuthenticationFailedException;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.model.password.PasswordDeviceServiceRemote;
import net.link.safeonline.model.password.PasswordManager;
import net.link.safeonline.service.NodeMappingService;
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

    @EJB(mappedName = NodeMappingService.JNDI_BINDING)
    private NodeMappingService  nodeMappingService;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService      subjectService;

    @EJB(mappedName = PasswordManager.JNDI_BINDING)
    private PasswordManager     passwordManager;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO        attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO    attributeTypeDAO;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger securityAuditLogger;

    @EJB(mappedName = HistoryDAO.JNDI_BINDING)
    private HistoryDAO          historyDAO;


    private AttributeEntity getDisableAttribute(SubjectEntity subject)
            throws DeviceRegistrationNotFoundException {

        AttributeTypeEntity disableAttributeType = attributeTypeDAO.findAttributeType(PasswordConstants.PASSWORD_DEVICE_DISABLE_ATTRIBUTE);
        List<AttributeEntity> disableAttributes = attributeDAO.listAttributes(subject, disableAttributeType);

        if (disableAttributes.isEmpty())
            throw new DeviceRegistrationNotFoundException();

        return disableAttributes.get(0);
    }

    /**
     * {@inheritDoc}
     */
    public void authenticate(String userId, String password)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException, DeviceAuthenticationException {

        LOG.debug("authenticate \"" + userId + "\"");

        SubjectEntity subject = subjectService.getSubject(userId);
        if (true == getDisableAttribute(subject).getBooleanValue())
            throw new DeviceDisabledException();

        if (false == passwordManager.validatePassword(subject, password)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect password");
            throw new DeviceAuthenticationException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void register(String nodeName, String userId, String password)
            throws NodeNotFoundException {

        LOG.debug("register password for \"" + userId + "\"");

        // Check through node mapping if subject exists, if not, it is created.
        SubjectEntity subject = nodeMappingService.getSubject(userId, nodeName);

        passwordManager.registerPassword(subject, password);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID));
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String userId)
            throws SubjectNotFoundException {

        LOG.debug("remove password for " + userId);
        SubjectEntity subject = subjectService.getSubject(userId);

        passwordManager.removePassword(subject); // FIXME: remove subject mapping

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REMOVAL, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                PasswordConstants.PASSWORD_DEVICE_ID));
    }

    /**
     * {@inheritDoc}
     * 
     * @throws AuthenticationFailedException
     */
    public void update(String userId, String oldPassword, String newPassword)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException, AuthenticationFailedException {

        LOG.debug("update password for \"" + userId + "\"");
        SubjectEntity subject = subjectService.getSubject(userId);

        if (true == getDisableAttribute(subject).getBooleanValue())
            throw new DeviceDisabledException();

        if (false == passwordManager.validatePassword(subject, oldPassword))
            throw new AuthenticationFailedException("Invalid password");

        passwordManager.updatePassword(subject, oldPassword, newPassword);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_UPDATE, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                PasswordConstants.PASSWORD_DEVICE_ID));
    }

    /**
     * {@inheritDoc}
     */
    public void enable(String userId, String password)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, AuthenticationFailedException {

        SubjectEntity subject = subjectService.getSubject(userId);

        LOG.debug("enable password for \"" + subject.getUserId() + "\"");

        if (!passwordManager.validatePassword(subject, password))
            throw new AuthenticationFailedException("Invalid password");

        getDisableAttribute(subject).setValue(false);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_ENABLE, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                PasswordConstants.PASSWORD_DEVICE_ID));
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = subjectService.getSubject(userId);

        LOG.debug("disable password for \"" + subject.getUserId() + "\"");

        getDisableAttribute(subject).setValue(true);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_DISABLE, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                PasswordConstants.PASSWORD_DEVICE_ID));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPasswordConfigured(String userId)
            throws SubjectNotFoundException {

        SubjectEntity subject = subjectService.getSubject(userId);

        return passwordManager.isPasswordConfigured(subject);
    }
}
