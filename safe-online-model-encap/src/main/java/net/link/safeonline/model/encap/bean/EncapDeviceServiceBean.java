/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.encap.bean;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.model.encap.EncapDeviceServiceRemote;
import net.link.safeonline.model.encap.MobileManager;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


@Stateless
@LocalBinding(jndiBinding = EncapDeviceService.JNDI_BINDING)
@RemoteBinding(jndiBinding = EncapDeviceServiceRemote.JNDI_BINDING)
public class EncapDeviceServiceBean implements EncapDeviceService, EncapDeviceServiceRemote {

    private static final Log       LOG = LogFactory.getLog(EncapDeviceServiceBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager          entityManager;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService         subjectService;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO   subjectIdentifierDAO;

    @EJB(mappedName = MobileManager.JNDI_BINDING)
    private MobileManager          mobileManager;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO              deviceDAO;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger    securityAuditLogger;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        this.attributeManager = new AttributeManagerLWBean(this.attributeDAO);
    }

    public void checkMobile(String mobile)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException, DeviceDisabledException {

        // check registration exists
        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        // check registration not disabled
        AttributeTypeEntity deviceAttributeType = this.attributeTypeDAO.getAttributeType(EncapConstants.ENCAP_DEVICE_ATTRIBUTE);
        AttributeTypeEntity mobileAttributeType = this.attributeTypeDAO.getAttributeType(EncapConstants.ENCAP_MOBILE_ATTRIBUTE);
        AttributeTypeEntity deviceDisableAttributeType = this.attributeTypeDAO
                                                                              .getAttributeType(EncapConstants.ENCAP_DEVICE_DISABLE_ATTRIBUTE);

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

    public String authenticate(String mobile, String challengeId, String mobileOTP)
            throws MalformedURLException, MobileException, SubjectNotFoundException, MobileAuthenticationException {

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        boolean result = authenicateEncap(challengeId, mobileOTP);
        if (false == result) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect mobile token");
            throw new MobileAuthenticationException();
        }
        return subject.getUserId();
    }

    public String register(String mobile, String sessionId)
            throws MalformedURLException, MobileException, MobileRegistrationException {

        String activationCode = this.mobileManager.activate(mobile, sessionId);
        if (null == activationCode)
            throw new MobileRegistrationException();
        return activationCode;
    }

    public boolean authenicateEncap(String challengeId, String mobileOTP)
            throws MalformedURLException, MobileException {

        return this.mobileManager.verifyOTP(challengeId, mobileOTP);
    }

    public void commitRegistration(String userId, String mobile)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException {

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);
        if (null != subject) {
            removeRegistration(subject, mobile);
            // flush and clear to commit and release the removed entities.
            this.entityManager.flush();
            this.entityManager.clear();
        }
        subject = this.subjectService.findSubject(userId);
        if (null == subject) {
            subject = this.subjectService.addSubjectWithoutLogin(userId);
        }

        setMobile(subject, mobile);

        this.subjectIdentifierDAO.addSubjectIdentifier(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile, subject);

    }

    private void setMobile(SubjectEntity subject, String mobile)
            throws AttributeTypeNotFoundException {

        AttributeTypeEntity deviceAttributeType = this.attributeTypeDAO.getAttributeType(EncapConstants.ENCAP_DEVICE_ATTRIBUTE);
        AttributeTypeEntity mobileAttributeType = this.attributeTypeDAO.getAttributeType(EncapConstants.ENCAP_MOBILE_ATTRIBUTE);
        AttributeTypeEntity deviceDisableAttributeType = this.attributeTypeDAO
                                                                              .getAttributeType(EncapConstants.ENCAP_DEVICE_DISABLE_ATTRIBUTE);

        AttributeEntity mobileAttribute = this.attributeDAO.addAttribute(mobileAttributeType, subject);
        mobileAttribute.setStringValue(mobile);
        AttributeEntity deviceDisableAttribute = this.attributeDAO.addAttribute(deviceDisableAttributeType, subject);
        deviceDisableAttribute.setBooleanValue(false);

        AttributeEntity deviceAttribute = this.attributeDAO.addAttribute(deviceAttributeType, subject);
        deviceAttribute.setStringValue(UUID.randomUUID().toString());
        List<AttributeEntity> deviceAttributeMembers = new LinkedList<AttributeEntity>();
        deviceAttributeMembers.add(mobileAttribute);
        deviceAttributeMembers.add(deviceDisableAttribute);
        deviceAttribute.setMembers(deviceAttributeMembers);
    }

    public void removeEncapMobile(String mobile)
            throws MalformedURLException, MobileException {

        this.mobileManager.remove(mobile);
    }

    public void remove(String userId, String mobile)
            throws MobileException, MalformedURLException, SubjectNotFoundException, AttributeTypeNotFoundException,
            AttributeNotFoundException {

        removeEncapMobile(mobile);

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);
        if (null == subject)
            throw new MobileException("device registration not found");

        removeRegistration(subject, mobile);
    }

    private void removeRegistration(SubjectEntity subject, String mobile)
            throws AttributeTypeNotFoundException, AttributeNotFoundException {

        AttributeTypeEntity deviceAttributeType = this.attributeTypeDAO.getAttributeType(EncapConstants.ENCAP_DEVICE_ATTRIBUTE);
        AttributeTypeEntity mobileAttributeType = this.attributeTypeDAO.getAttributeType(EncapConstants.ENCAP_MOBILE_ATTRIBUTE);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, deviceAttributeType);
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = this.attributeDAO.findAttribute(subject, mobileAttributeType,
                    deviceAttribute.getAttributeIndex());
            if (mobileAttribute.getStringValue().equals(mobile)) {
                LOG.debug("remove attribute");
                this.attributeManager.removeAttribute(deviceAttributeType, deviceAttribute.getAttributeIndex(), subject);
                break;
            }
        }

        this.subjectIdentifierDAO.removeSubjectIdentifier(subject, EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);

    }

    public String requestOTP(String mobile)
            throws MalformedURLException, MobileException {

        return this.mobileManager.requestOTP(mobile);
    }

    /**
     * {@inheritDoc}
     */
    public List<AttributeDO> getMobiles(String userId, Locale locale)
            throws SubjectNotFoundException, DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(EncapConstants.ENCAP_DEVICE_ID);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        List<AttributeDO> attributes = new LinkedList<AttributeDO>();

        String humanReadableName = null;
        String description = null;
        AttributeTypeDescriptionEntity attributeTypeDescription = findAttributeTypeDescription(device.getUserAttributeType(), locale);
        if (null != attributeTypeDescription) {
            humanReadableName = attributeTypeDescription.getName();
            description = attributeTypeDescription.getDescription();
        }

        List<AttributeEntity> userAttributes = this.attributeDAO.listAttributes(subject, device.getUserAttributeType());
        for (AttributeEntity userAttribute : userAttributes) {
            attributes
                      .add(new AttributeDO(device.getUserAttributeType().getName(), device.getUserAttributeType().getType(), true,
                              userAttribute.getAttributeIndex(), humanReadableName, description, false, false,
                              userAttribute.getStringValue(), false));
        }
        return attributes;
    }

    private AttributeTypeDescriptionEntity findAttributeTypeDescription(AttributeTypeEntity attributeType, Locale locale) {

        if (null == locale)
            return null;
        String language = locale.getLanguage();
        LOG.debug("trying language: " + language);
        AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO.findDescription(new AttributeTypeDescriptionPK(
                attributeType.getName(), language));
        return attributeTypeDescription;
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String mobile)
            throws SubjectNotFoundException, DeviceNotFoundException, DeviceRegistrationNotFoundException, MalformedURLException,
            MobileException {

        DeviceEntity device = this.deviceDAO.getDevice(EncapConstants.ENCAP_DEVICE_ID);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = this.attributeDAO.findAttribute(subject, EncapConstants.ENCAP_MOBILE_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (mobileAttribute.getStringValue().equals(mobile)) {
                LOG.debug("disable mobile " + mobile);
                AttributeEntity disableAttribute = this.attributeDAO.findAttribute(subject, device.getDisableAttributeType(),
                        deviceAttribute.getAttributeIndex());
                this.mobileManager.lock(mobile);
                disableAttribute.setBooleanValue(true);

                return;
            }
        }

        throw new DeviceRegistrationNotFoundException();

    }

    /**
     * {@inheritDoc}
     */
    public void enable(String userId, String mobile)
            throws SubjectNotFoundException, DeviceNotFoundException, DeviceRegistrationNotFoundException, MalformedURLException,
            MobileException {

        DeviceEntity device = this.deviceDAO.getDevice(EncapConstants.ENCAP_DEVICE_ID);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = this.attributeDAO.findAttribute(subject, EncapConstants.ENCAP_MOBILE_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (mobileAttribute.getStringValue().equals(mobile)) {
                LOG.debug("enable mobile " + mobile);
                AttributeEntity disableAttribute = this.attributeDAO.findAttribute(subject, device.getDisableAttributeType(),
                        deviceAttribute.getAttributeIndex());
                this.mobileManager.unLock(mobile);
                disableAttribute.setBooleanValue(false);

                return;
            }
        }

        throw new DeviceRegistrationNotFoundException();

    }

}
