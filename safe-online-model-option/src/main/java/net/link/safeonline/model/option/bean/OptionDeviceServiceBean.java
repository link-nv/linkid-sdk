/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.option.bean;

import java.util.LinkedList;
import java.util.List;
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
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.option.OptionConstants;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.model.option.OptionDeviceServiceRemote;
import net.link.safeonline.model.option.exception.OptionAuthenticationException;
import net.link.safeonline.model.option.exception.OptionRegistrationException;
import net.link.safeonline.service.SubjectService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


/**
 * <h2>{@link OptionDeviceServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 8, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
@Stateless
@LocalBinding(jndiBinding = OptionDeviceService.JNDI_BINDING)
@RemoteBinding(jndiBinding = OptionDeviceServiceRemote.JNDI_BINDING)
public class OptionDeviceServiceBean implements OptionDeviceService, OptionDeviceServiceRemote {

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO   subjectIdentifierDAO;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO              deviceDAO;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger    securityAuditLogger;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService         subjectService;

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager          entityManager;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        this.attributeManager = new AttributeManagerLWBean(this.attributeDAO);
    }

    /**
     * {@inheritDoc}
     */
    public String authenticate(String imei, String pin)
            throws SubjectNotFoundException, OptionAuthenticationException, OptionRegistrationException, AttributeTypeNotFoundException,
            AttributeNotFoundException, DeviceDisabledException {

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
        if (null == subject)
            throw new SubjectNotFoundException();

        // check registration not disabled
        AttributeTypeEntity deviceAttributeType = this.attributeTypeDAO.getAttributeType(OptionConstants.OPTION_DEVICE_ATTRIBUTE);
        AttributeTypeEntity imeiAttributeType = this.attributeTypeDAO.getAttributeType(OptionConstants.IMEI_OPTION_ATTRIBUTE);
        AttributeTypeEntity deviceDisableAttributeType = this.attributeTypeDAO
                                                                              .getAttributeType(OptionConstants.OPTION_DEVICE_DISABLE_ATTRIBUTE);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, deviceAttributeType);
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity imeiAttribute = this.attributeDAO
                                                             .findAttribute(subject, imeiAttributeType, deviceAttribute.getAttributeIndex());
            if (imeiAttribute.getStringValue().equals(imei)) {
                AttributeEntity disableAttribute = this.attributeDAO.getAttribute(deviceDisableAttributeType, subject,
                        deviceAttribute.getAttributeIndex());
                if (true == disableAttribute.getBooleanValue())
                    throw new DeviceDisabledException();
            }
        }

        authenticate(subject, imei, pin);

        return subject.getUserId();
    }

    /**
     * {@inheritDoc}
     */
    public void register(String userId, String imei, String pin)
            throws OptionAuthenticationException, OptionRegistrationException, AttributeTypeNotFoundException, AttributeNotFoundException,
            DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
        if (null != subject) {
            authenticate(subject, imei, pin);
            remove(userId, imei);
            this.entityManager.flush();
            this.entityManager.clear();
        }

        subject = this.subjectService.findSubject(userId);
        if (null == subject) {
            subject = this.subjectService.addSubjectWithoutLogin(userId);
        }

        AttributeTypeEntity imeiType = this.attributeTypeDAO.findAttributeType(OptionConstants.IMEI_OPTION_ATTRIBUTE);
        AttributeTypeEntity pinType = this.attributeTypeDAO.findAttributeType(OptionConstants.PIN_OPTION_ATTRIBUTE);
        AttributeTypeEntity deviceAttributeType = this.attributeTypeDAO.getAttributeType(OptionConstants.OPTION_DEVICE_ATTRIBUTE);
        AttributeTypeEntity deviceDisableAttributeType = this.attributeTypeDAO
                                                                              .getAttributeType(OptionConstants.OPTION_DEVICE_DISABLE_ATTRIBUTE);

        AttributeEntity imeiAttribute = this.attributeDAO.addAttribute(imeiType, subject);
        imeiAttribute.setStringValue(imei);

        AttributeEntity pinAttribute = this.attributeDAO.addAttribute(pinType, subject);
        pinAttribute.setStringValue(pin);

        AttributeEntity deviceDisableAttribute = this.attributeDAO.addAttribute(deviceDisableAttributeType, subject);
        deviceDisableAttribute.setBooleanValue(false);

        AttributeEntity deviceAttribute = this.attributeDAO.addAttribute(deviceAttributeType, subject);
        deviceAttribute.setStringValue(UUID.randomUUID().toString());
        List<AttributeEntity> deviceAttributeMembers = new LinkedList<AttributeEntity>();
        deviceAttributeMembers.add(imeiAttribute);
        deviceAttributeMembers.add(pinAttribute);
        deviceAttributeMembers.add(deviceDisableAttribute);
        deviceAttribute.setMembers(deviceAttributeMembers);

        this.subjectIdentifierDAO.addSubjectIdentifier(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei, subject);

    }

    private void authenticate(SubjectEntity subject, String givenImei, String givenPin)
            throws OptionRegistrationException, OptionAuthenticationException {

        AttributeEntity storedImei = this.attributeDAO.findAttribute(OptionConstants.IMEI_OPTION_ATTRIBUTE, subject);
        AttributeEntity storedPin = this.attributeDAO.findAttribute(OptionConstants.PIN_OPTION_ATTRIBUTE, subject);

        if (null == storedImei || null == storedPin)
            throw new OptionRegistrationException();
        if (!storedImei.getStringValue().equals(givenImei))
            throw new OptionRegistrationException();
        if (!storedPin.getStringValue().equals(givenPin)) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect PIN");
            throw new OptionAuthenticationException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String imei)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(OptionConstants.OPTION_DEVICE_ID);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity imeiAttribute = this.attributeDAO.findAttribute(subject, OptionConstants.IMEI_OPTION_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (imeiAttribute.getStringValue().equals(imei)) {
                AttributeEntity disableAttribute = this.attributeDAO.findAttribute(subject, device.getDisableAttributeType(),
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
    public void enable(String userId, String imei, String pin)
            throws OptionAuthenticationException, OptionRegistrationException, SubjectNotFoundException,
            DeviceRegistrationNotFoundException, DeviceNotFoundException {

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
        if (null == subject)
            throw new SubjectNotFoundException();

        authenticate(subject, imei, pin);

        if (!subject.getUserId().equals(userId))
            throw new OptionRegistrationException();

        DeviceEntity device = this.deviceDAO.getDevice(OptionConstants.OPTION_DEVICE_ID);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity imeiAttribute = this.attributeDAO.findAttribute(subject, OptionConstants.IMEI_OPTION_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (imeiAttribute.getStringValue().equals(imei)) {
                AttributeEntity disableAttribute = this.attributeDAO.findAttribute(subject, device.getDisableAttributeType(),
                        deviceAttribute.getAttributeIndex());
                disableAttribute.setBooleanValue(false);
                return;
            }
        }

        throw new DeviceRegistrationNotFoundException();

    }

    /**
     * {@inheritDoc}
     */
    public void remove(String userId, String imei)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException, AttributeTypeNotFoundException,
            AttributeNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(OptionConstants.OPTION_DEVICE_ID);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity imeiAttribute = this.attributeDAO.findAttribute(subject, OptionConstants.IMEI_OPTION_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (imeiAttribute.getStringValue().equals(imei)) {

                this.attributeManager.removeAttribute(device.getAttributeType(), deviceAttribute.getAttributeIndex(), subject);
                this.subjectIdentifierDAO.removeSubjectIdentifier(subject, OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
                return;
            }
        }
        throw new DeviceRegistrationNotFoundException();
    }

}
