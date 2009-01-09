/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms.bean;

import java.net.ConnectException;
import java.util.List;

import javax.ejb.EJB;
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
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceServiceRemote;
import net.link.safeonline.model.otpoversms.OtpOverSmsManager;
import net.link.safeonline.model.otpoversms.OtpService;
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

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService       subjectService;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO subjectIdentifierDAO;

    @EJB(mappedName = OtpOverSmsManager.JNDI_BINDING)
    private OtpOverSmsManager    otpOverSmsManager;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO         attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO     attributeTypeDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO            deviceDAO;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger  securityAuditLogger;


    public void checkMobile(String mobile)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException, DeviceDisabledException {

        // check registration exists
        SubjectEntity subject = subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        // check registration not disabled
        AttributeTypeEntity deviceAttributeType = attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE);
        AttributeTypeEntity mobileAttributeType = attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE);
        AttributeTypeEntity deviceDisableAttributeType = attributeTypeDAO
                                                                              .getAttributeType(OtpOverSmsConstants.OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE);

        List<AttributeEntity> deviceAttributes = attributeDAO.listAttributes(subject, deviceAttributeType);
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = attributeDAO.findAttribute(subject, mobileAttributeType,
                    deviceAttribute.getAttributeIndex());
            if (mobileAttribute.getStringValue().equals(mobile)) {
                AttributeEntity disableAttribute = attributeDAO.getAttribute(deviceDisableAttributeType, subject,
                        deviceAttribute.getAttributeIndex());
                if (true == disableAttribute.getBooleanValue())
                    throw new DeviceDisabledException();
            }
        }
    }

    public String authenticate(String mobile, String pin)
            throws DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("authenticate otp over sms device mobile=" + mobile);

        SubjectEntity subject = subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        boolean validationResult = false;
        try {
            validationResult = otpOverSmsManager.validatePin(subject, mobile, pin);
        } catch (DeviceNotFoundException e) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "otp over sms device not found");
            throw e;
        }

        if (!validationResult) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect pin");
            return null;
        }
        return subject.getUserId();
    }

    public void register(String userId, String mobile, String pin)
            throws SubjectNotFoundException, DeviceNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException,
            PermissionDeniedException {

        LOG.debug("register otp over sms device for \"" + userId + "\" mobile=" + mobile);

        SubjectEntity subject = subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
        if (null != subject) {
            otpOverSmsManager.removeMobile(subject, mobile);
            subjectIdentifierDAO.removeSubjectIdentifier(subject, OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
            // flush and clear to commit and release the removed entities.
            entityManager.flush();
            entityManager.clear();
        }
        subject = subjectService.findSubject(userId);
        if (null == subject) {
            subject = subjectService.addSubjectWithoutLogin(userId);
        }

        otpOverSmsManager.registerMobile(subject, mobile, pin);

        subjectIdentifierDAO.addSubjectIdentifier(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile, subject);

    }

    public void remove(String userId, String mobile)
            throws DeviceNotFoundException, SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException,
            DeviceDisabledException {

        checkMobile(mobile);

        LOG.debug("remove otp over sms device for " + userId + " mobile=" + mobile);
        SubjectEntity subject = subjectService.getSubject(userId);

        otpOverSmsManager.removeMobile(subject, mobile);

        subjectIdentifierDAO.removeSubjectIdentifier(subject, OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);

        return;

    }

    public boolean update(String userId, String mobile, String oldPin, String newPin)
            throws DeviceNotFoundException, SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException,
            DeviceDisabledException {

        checkMobile(mobile);

        LOG.debug("update pin for otp over sms device for \"" + userId + "\" mobile=" + mobile);
        SubjectEntity subject = subjectService.getSubject(userId);

        if (!otpOverSmsManager.changePin(subject, mobile, oldPin, newPin))
            return false;

        return true;

    }

    /**
     * {@inheritDoc}
     */
    public boolean enable(String userId, String mobile, String pin)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException, AttributeTypeNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID);
        SubjectEntity subject = subjectService.getSubject(userId);

        if (!otpOverSmsManager.validatePin(subject, mobile, pin))
            return false;

        AttributeTypeEntity attemptsAttributeType = attributeTypeDAO
                                                                         .getAttributeType(OtpOverSmsConstants.OTPOVERSMS_PIN_ATTEMPTS_ATTRIBUTE);

        List<AttributeEntity> deviceAttributes = attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = attributeDAO.findAttribute(subject, OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (mobileAttribute.getStringValue().equals(mobile)) {
                LOG.debug("disable mobile " + mobile);
                AttributeEntity disableAttribute = attributeDAO.findAttribute(subject, device.getDisableAttributeType(),
                        deviceAttribute.getAttributeIndex());
                if (true == disableAttribute.getBooleanValue()) {
                    AttributeEntity attemptsAttribute = attributeDAO.findAttribute(subject, attemptsAttributeType,
                            deviceAttribute.getAttributeIndex());
                    attemptsAttribute.setIntegerValue(0);
                }
                disableAttribute.setBooleanValue(false);

                return true;
            }
        }

        throw new DeviceRegistrationNotFoundException();
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String mobile)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID);
        SubjectEntity subject = subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = attributeDAO.findAttribute(subject, OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (mobileAttribute.getStringValue().equals(mobile)) {
                LOG.debug("disable mobile " + mobile);
                AttributeEntity disableAttribute = attributeDAO.findAttribute(subject, device.getDisableAttributeType(),
                        deviceAttribute.getAttributeIndex());
                disableAttribute.setBooleanValue(true);

                return;
            }
        }

        throw new DeviceRegistrationNotFoundException();
    }

    /**
     * {@inheritDoc}
     */
    public void requestOtp(HttpSession httpSession, String mobile)
            throws ConnectException, SafeOnlineResourceException {

        OtpService otpService = EjbUtils.getEJB(OtpService.JNDI_BINDING, OtpService.class);
        otpService.requestOtp(mobile);

        httpSession.setAttribute(OTP_SERVICE_ATTRIBUTE, otpService);

    }

    /**
     * {@inheritDoc}
     */
    public boolean verifyOtp(HttpSession httpSession, String mobile, String otp)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException, DeviceDisabledException {

        checkMobile(mobile);

        OtpService otpService = (OtpService) httpSession.getAttribute(OTP_SERVICE_ATTRIBUTE);
        return otpService.verifyOtp(otp);
    }

    /**
     * {@inheritDoc}
     */
    public boolean verifyOtp(HttpSession httpSession, String otp) {

        OtpService otpService = (OtpService) httpSession.getAttribute(OTP_SERVICE_ATTRIBUTE);
        return otpService.verifyOtp(otp);
    }
}
