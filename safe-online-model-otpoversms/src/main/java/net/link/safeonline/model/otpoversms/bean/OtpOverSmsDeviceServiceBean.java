/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms.bean;

import java.net.ConnectException;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.device.backend.OtpService;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceServiceRemote;
import net.link.safeonline.model.otpoversms.OtpOverSmsManager;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


@Stateless
@LocalBinding(jndiBinding = OtpOverSmsDeviceService.JNDI_BINDING)
@RemoteBinding(jndiBinding = OtpOverSmsDeviceServiceRemote.JNDI_BINDING)
public class OtpOverSmsDeviceServiceBean implements OtpOverSmsDeviceService, OtpOverSmsDeviceServiceRemote {

    private final static Log     LOG                   = LogFactory.getLog(OtpOverSmsDeviceServiceBean.class);

    public static final String   OTP_SERVICE_ATTRIBUTE = "otpService";

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager        entityManager;

    @EJB
    private SubjectService       subjectService;

    @EJB
    private SubjectIdentifierDAO subjectIdentifierDAO;

    @EJB
    private OtpOverSmsManager    otpOverSmsManager;

    @EJB
    private AttributeDAO         attributeDAO;

    @EJB
    private AttributeTypeDAO     attributeTypeDAO;

    @EJB
    private DeviceDAO            deviceDAO;

    @EJB
    private SecurityAuditLogger  securityAuditLogger;

    @EJB
    private HistoryDAO           historyDAO;


    public void checkMobile(String mobile)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException, DeviceDisabledException {

        // check registration exists
        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        // check registration not disabled
        AttributeTypeEntity deviceAttributeType = this.attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE);
        AttributeTypeEntity mobileAttributeType = this.attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE);
        AttributeTypeEntity deviceDisableAttributeType = this.attributeTypeDAO
                                                                              .getAttributeType(OtpOverSmsConstants.OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, deviceAttributeType);
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = this.attributeDAO.findAttribute(subject, mobileAttributeType,
                    deviceAttribute.getAttributeIndex());
            if (mobileAttribute.getStringValue().equals(mobile)) {
                AttributeEntity disableAttribute = this.attributeDAO.getAttribute(deviceDisableAttributeType, subject,
                        deviceAttribute.getAttributeIndex());
                if (true == disableAttribute.getBooleanValue())
                    throw new DeviceDisabledException();
            }
        }
    }

    public String authenticate(String mobile, String pin)
            throws DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("authenticate otp over sms device mobile=" + mobile);

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        boolean validationResult = false;
        try {
            validationResult = this.otpOverSmsManager.validatePin(subject, mobile, pin);
        } catch (DeviceNotFoundException e) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "otp over sms device not found");
            throw e;
        }

        if (!validationResult) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect pin");
            return null;
        }
        return subject.getUserId();
    }

    public void register(String userId, String mobile, String pin)
            throws SubjectNotFoundException, DeviceNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException {

        LOG.debug("register otp over sms device for \"" + userId + "\" mobile=" + mobile);

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
        if (null != subject) {
            this.otpOverSmsManager.removeMobile(subject, mobile);
            this.subjectIdentifierDAO.removeSubjectIdentifier(subject, OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
            // flush and clear to commit and release the removed entities.
            this.entityManager.flush();
            this.entityManager.clear();
        }
        subject = this.subjectService.findSubject(userId);
        if (null == subject) {
            subject = this.subjectService.addSubjectWithoutLogin(userId);
        }

        try {
            this.otpOverSmsManager.registerMobile(subject, mobile, pin);
        } catch (PermissionDeniedException e) {
            throw new EJBException("Not allowed to set mobile");
        }

        this.subjectIdentifierDAO.addSubjectIdentifier(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile, subject);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID));

    }

    public void remove(String userId, String mobile, String pin)
            throws DeviceNotFoundException, PermissionDeniedException, SubjectNotFoundException, AttributeTypeNotFoundException {

        LOG.debug("remove otp over sms device for " + userId + " mobile=" + mobile);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        if (!this.otpOverSmsManager.validatePin(subject, mobile, pin))
            throw new PermissionDeniedException("pin mismatch");

        this.otpOverSmsManager.removeMobile(subject, mobile);

        this.subjectIdentifierDAO.removeSubjectIdentifier(subject, OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REMOVAL, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID));

    }

    public void update(String userId, String mobile, String oldPin, String newPin)
            throws PermissionDeniedException, DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("update pin for otp over sms device for \"" + userId + "\" mobile=" + mobile);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        this.otpOverSmsManager.changePin(subject, mobile, oldPin, newPin);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_UPDATE, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID));

    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String mobile)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = this.attributeDAO.findAttribute(subject, OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (mobileAttribute.getStringValue().equals(mobile)) {
                LOG.debug("disable mobile " + mobile);
                AttributeEntity disableAttribute = this.attributeDAO.findAttribute(subject, device.getDisableAttributeType(),
                        deviceAttribute.getAttributeIndex());
                disableAttribute.setBooleanValue(!disableAttribute.getBooleanValue());
                return;
            }
        }

        throw new DeviceRegistrationNotFoundException();
    }

    /**
     * {@inheritDoc}
     */
    public void requestOtp(HttpSession httpSession, String mobile)
            throws ConnectException {

        OtpService otpService = EjbUtils.getEJB(OtpService.JNDI_BINDING, OtpService.class);
        otpService.requestOtp(mobile);

        httpSession.setAttribute(OTP_SERVICE_ATTRIBUTE, otpService);

    }

    /**
     * {@inheritDoc}
     */
    public boolean verifyOtp(HttpSession httpSession, String otp) {

        OtpService otpService = (OtpService) httpSession.getAttribute(OTP_SERVICE_ATTRIBUTE);
        return otpService.verifyOtp(otp);
    }
}
