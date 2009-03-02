/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms.bean;

import java.security.SecureRandom;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.PostActivate;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.ResourceAuditLoggerInterceptor;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceServiceRemote;
import net.link.safeonline.model.otpoversms.OtpOverSmsManager;
import net.link.safeonline.osgi.OSGIConstants;
import net.link.safeonline.osgi.OSGIService;
import net.link.safeonline.osgi.OSGIStartable;
import net.link.safeonline.osgi.OSGIConstants.OSGIServiceType;
import net.link.safeonline.osgi.sms.SmsService;
import net.link.safeonline.osgi.sms.exception.SmsServiceException;
import net.link.safeonline.service.NodeMappingService;
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

    private final static Log                 LOG                   = LogFactory.getLog(OtpOverSmsDeviceServiceBean.class);

    public static final String               OTP_SERVICE_ATTRIBUTE = "otpService";

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                    entityManager;

    @EJB(mappedName = HistoryDAO.JNDI_BINDING)
    private HistoryDAO                       historyDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService                   subjectService;

    @EJB(mappedName = NodeMappingService.JNDI_BINDING)
    private NodeMappingService               nodeMappingService;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO             subjectIdentifierDAO;

    @EJB(mappedName = OtpOverSmsManager.JNDI_BINDING)
    private OtpOverSmsManager                otpOverSmsManager;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO                     attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO                 attributeTypeDAO;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger              securityAuditLogger;

    @Configurable(name = OSGIConstants.SMS_SERVICE_IMPL_NAME, group = OSGIConstants.SMS_SERVICE_GROUP_NAME, multipleChoice = true)
    private String                           smsServiceName;

    @EJB(mappedName = OSGIStartable.JNDI_BINDING)
    private OSGIStartable                    osgiStartable;

    private String                           challengeMobile;

    private String                           expectedOtp;

    private transient AttributeManagerLWBean attributeManager;


    @PostActivate
    @PostConstruct
    public void activateCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        attributeManager = new AttributeManagerLWBean(attributeDAO, attributeTypeDAO);
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
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String authenticate(String pin, String otp)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException, DeviceAuthenticationException {

        LOG.debug("authenticate otp over sms device mobile=" + challengeMobile);

        SubjectEntity subject = subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, challengeMobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        if (false == verifyOtp(otp)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect otp");
            throw new DeviceAuthenticationException();
        }

        if (true == getDisableAttribute(subject, challengeMobile).getBooleanValue())
            throw new DeviceDisabledException();

        if (false == otpOverSmsManager.validatePin(subject, challengeMobile, pin)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect pin");
            throw new DeviceAuthenticationException();
        }

        // Success, reset the challenge data.
        challengeMobile = null;
        expectedOtp = null;

        return subject.getUserId();
    }

    /**
     * {@inheritDoc}
     */
    public void register(String nodeName, String userId, String pin, String otp)
            throws DeviceAuthenticationException, DeviceAuthenticationException {

        if (false == verifyOtp(otp))
            throw new DeviceAuthenticationException();

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

        /*
         * Check through node mapping if subject exists, if not, it is created.
         */
        try {
            subject = nodeMappingService.getSubject(userId, nodeName);
        } catch (NodeNotFoundException e) {
            throw new DeviceAuthenticationException(e);
        }

        // Register the mobile with that subject and map the mobile to the subject.
        otpOverSmsManager.registerMobile(subject, challengeMobile, pin);
        subjectIdentifierDAO.addSubjectIdentifier(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, challengeMobile, subject);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID));

        // Success, reset the challenge data.
        challengeMobile = null;
        expectedOtp = null;
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

        // flush and clear to commit and release the removed entities.
        entityManager.flush();
        entityManager.clear();

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REMOVAL, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID));
    }

    /**
     * {@inheritDoc}
     */
    public void update(String userId, String oldPin, String newPin, String otp)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException, DeviceAuthenticationException {

        LOG.debug("update pin for otp over sms device for \"" + userId + "\" mobile=" + challengeMobile);
        SubjectEntity subject = subjectService.getSubject(userId);

        if (true == getDisableAttribute(subject, challengeMobile).getBooleanValue())
            throw new DeviceDisabledException();

        if (false == verifyOtp(otp)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect otp");
            throw new DeviceAuthenticationException("Invalid OTP");
        }

        if (!otpOverSmsManager.validatePin(subject, challengeMobile, oldPin)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect pin");
            throw new DeviceAuthenticationException("Invalid PIN");
        }

        otpOverSmsManager.updatePin(subject, challengeMobile, oldPin, newPin);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_UPDATE, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID));

        // Success, reset the challenge data.
        challengeMobile = null;
        expectedOtp = null;
    }

    /**
     * {@inheritDoc}
     */
    public void enable(String userId, String pin, String otp)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceAuthenticationException {

        SubjectEntity subject = subjectService.getSubject(userId);

        if (false == verifyOtp(otp))
            throw new DeviceAuthenticationException("Invalid OTP");

        if (!otpOverSmsManager.validatePin(subject, challengeMobile, pin)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect pin");
            throw new DeviceAuthenticationException("Invalid PIN");
        }

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

            historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_ENABLE, Collections.singletonMap(
                    SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID));

            // Success, reset the challenge data.
            challengeMobile = null;
            expectedOtp = null;
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for OtpOverSMS device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = subjectIdentifierDAO.findSubject(OtpOverSmsConstants.OTPOVERSMS_IDENTIFIER_DOMAIN, mobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        AttributeEntity disableAttribute = getDisableAttribute(subject, mobile);
        disableAttribute.setValue(true);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_DISABLE, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID));
    }

    /**
     * {@inheritDoc}
     */
    public void requestOtp(String mobile)
            throws SmsServiceException, SafeOnlineResourceException, SubjectNotFoundException, DeviceRegistrationNotFoundException {

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
