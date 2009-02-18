/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms.bean;

import java.net.ConnectException;
import java.security.SecureRandom;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;
import javax.mail.AuthenticationFailedException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.ResourceAuditLoggerInterceptor;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceServiceRemote;
import net.link.safeonline.model.otpoversms.OtpOverSmsManager;
import net.link.safeonline.osgi.OSGIHostActivator;
import net.link.safeonline.osgi.OSGIService;
import net.link.safeonline.osgi.OSGIStartable;
import net.link.safeonline.osgi.OSGIHostActivator.OSGIServiceType;
import net.link.safeonline.osgi.sms.SmsService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


@Stateful
@LocalBinding(jndiBinding = OtpOverSmsDeviceService.JNDI_BINDING)
@RemoteBinding(jndiBinding = OtpOverSmsDeviceServiceRemote.JNDI_BINDING)
@Interceptors( { ConfigurationInterceptor.class, ResourceAuditLoggerInterceptor.class })
@Configurable
public class OtpOverSmsDeviceServiceBean implements OtpOverSmsDeviceService, OtpOverSmsDeviceServiceRemote {

    private final static Log       LOG                   = LogFactory.getLog(OtpOverSmsDeviceServiceBean.class);

    public static final String     OTP_SERVICE_ATTRIBUTE = "otpService";

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager          entityManager;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService         subjectService;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO   subjectIdentifierDAO;

    @EJB(mappedName = OtpOverSmsManager.JNDI_BINDING)
    private OtpOverSmsManager      otpOverSmsManager;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger    securityAuditLogger;

    @Configurable(name = OSGIHostActivator.SMS_SERVICE_IMPL_NAME, group = OSGIHostActivator.SMS_SERVICE_GROUP_NAME, multipleChoice = true)
    private String                 smsServiceName;

    @EJB(mappedName = OSGIStartable.JNDI_BINDING)
    private OSGIStartable          osgiStartable;

    private AttributeManagerLWBean attributeManager;

    private String                 challengeMobile;

    private String                 expectedOtp;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        attributeManager = new AttributeManagerLWBean(attributeDAO, attributeTypeDAO);
    }

    private AttributeEntity getDisableAttribute(String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        return getDisableAttribute(subject, mobile);
    }

    private AttributeEntity getDisableAttribute(SubjectEntity subject, String mobile)
            throws DeviceRegistrationNotFoundException {

        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE,
                    OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE, mobile);
            AttributeEntity disableAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE);

            return disableAttribute;
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for OtpOverSMS device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public String authenticate(String pin, String otp)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException {

        LOG.debug("authenticate otp over sms device mobile=" + challengeMobile);

        if (false == verifyOtp(otp))
            return null;

        SubjectEntity subject = subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, challengeMobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        if (true == getDisableAttribute(subject, challengeMobile).getBooleanValue())
            throw new DeviceDisabledException();

        if (false == otpOverSmsManager.validatePin(subject, challengeMobile, pin)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect pin");
            return null;
        }

        return subject.getUserId();
    }

    /**
     * {@inheritDoc}
     */
    public void register(String userId, String pin, String otp)
            throws PermissionDeniedException, AuthenticationFailedException {

        if (false == verifyOtp(otp))
            throw new AuthenticationFailedException();

        LOG.debug("register otp over sms device for \"" + userId + "\" mobile=" + challengeMobile);

        // Remove existing registration for this mobile.
        SubjectEntity subject = subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, challengeMobile);
        if (null != subject) {
            try {
                otpOverSmsManager.removeMobile(subject, challengeMobile);
            } catch (DeviceRegistrationNotFoundException e) {
            }

            subjectIdentifierDAO.removeSubjectIdentifier(subject, OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, challengeMobile);
            // flush and clear to commit and release the removed entities.
            entityManager.flush();
            entityManager.clear();
        }

        // Create a subject for this user if one doesn't exist already.
        subject = subjectService.findSubject(userId);
        if (null == subject) {
            subject = subjectService.addSubjectWithoutLogin(userId);
        }

        // Register the mobile with that subject and map the mobile to the subject.
        otpOverSmsManager.registerMobile(subject, challengeMobile, pin);
        subjectIdentifierDAO.addSubjectIdentifier(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, challengeMobile, subject);
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        LOG.debug("remove otp over sms device for " + userId + " mobile=" + mobile);
        SubjectEntity subject = subjectService.getSubject(userId);

        otpOverSmsManager.removeMobile(subject, mobile);
        subjectIdentifierDAO.removeSubjectIdentifier(subject, OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
    }

    /**
     * {@inheritDoc}
     */
    public void update(String userId, String otp, String oldPin, String newPin)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException, PermissionDeniedException {

        LOG.debug("update pin for otp over sms device for \"" + userId + "\" mobile=" + challengeMobile);
        SubjectEntity subject = subjectService.getSubject(userId);

        if (false == verifyOtp(otp))
            throw new PermissionDeniedException("Invalid OTP");

        if (true == getDisableAttribute(subject, challengeMobile).getBooleanValue())
            throw new DeviceDisabledException();

        if (!otpOverSmsManager.validatePin(subject, challengeMobile, oldPin))
            throw new PermissionDeniedException("Invalid PIN");

        otpOverSmsManager.updatePin(subject, challengeMobile, oldPin, newPin);
    }

    /**
     * {@inheritDoc}
     */
    public void enable(String userId, String otp, String pin)
            throws SubjectNotFoundException, AuthenticationFailedException, DeviceRegistrationNotFoundException, PermissionDeniedException {

        SubjectEntity subject = subjectService.getSubject(userId);

        if (false == verifyOtp(otp))
            throw new AuthenticationFailedException();

        if (!otpOverSmsManager.validatePin(subject, challengeMobile, pin))
            throw new PermissionDeniedException("Invalid PIN");

        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE,
                    OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE, challengeMobile);
            AttributeEntity disableAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE);
            AttributeEntity attemptsAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_PIN_ATTEMPTS_ATTRIBUTE);

            if (true == disableAttribute.getBooleanValue()) {
                attemptsAttribute.setValue(0);
                disableAttribute.setValue(false);
            }
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for OtpOverSMS device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        AttributeEntity disableAttribute = getDisableAttribute(mobile);
        disableAttribute.setValue(true);
    }

    /**
     * {@inheritDoc}
     */
    public void requestOtp(String mobile)
            throws ConnectException, SafeOnlineResourceException, SubjectNotFoundException, DeviceRegistrationNotFoundException {

        LOG.debug("request otp for mobile " + mobile + " using sms service: " + smsServiceName);

        SecureRandom random = new SecureRandom();
        expectedOtp = Integer.toString(Math.abs(random.nextInt()));

        challengeMobile = mobile;
        OSGIService osgiService = osgiStartable.getService(smsServiceName, OSGIServiceType.SMS_SERVICE);
        ((SmsService) osgiService.getService()).sendSms(challengeMobile, expectedOtp);
        osgiService.ungetService();
    }

    private boolean verifyOtp(String otp) {

        LOG.debug("verify otp " + otp);

        return isChallenged() && expectedOtp.equals(otp);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isChallenged() {

        return expectedOtp != null;
    }
}
