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
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.option.OptionConstants;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.model.option.OptionDeviceServiceRemote;
import net.link.safeonline.model.option.exception.OptionAuthenticationException;
import net.link.safeonline.model.option.exception.OptionRegistrationException;
import net.link.safeonline.service.SubjectService;


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
public class OptionDeviceServiceBean implements OptionDeviceService, OptionDeviceServiceRemote {

    @EJB
    private SubjectIdentifierDAO subjectIdentifierDAO;

    @EJB
    private AttributeDAO         attributeDAO;

    @EJB
    private AttributeTypeDAO     attributeTypeDAO;

    @EJB
    private DeviceDAO            deviceDAO;

    @EJB
    private SecurityAuditLogger  securityAuditLogger;

    @EJB
    private SubjectService       subjectService;

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager        entityManager;


    /**
     * {@inheritDoc}
     */
    public String authenticate(String imei, String pin) throws SubjectNotFoundException, OptionAuthenticationException,
                                                       OptionRegistrationException, AttributeTypeNotFoundException,
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
    public void register(String userId, String imei, String pin) throws OptionAuthenticationException, OptionRegistrationException,
                                                                AttributeTypeNotFoundException {

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
        if (null != subject) {
            authenticate(subject, imei, pin);
            removeRegistration(subject, imei);
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

        int attributeIdx = this.attributeDAO.listAttributes(subject, deviceAttributeType).size();

        AttributeEntity imeiAttribute = this.attributeDAO.findAttribute(subject, imeiType, attributeIdx);
        if (null == imeiAttribute) {
            imeiAttribute = this.attributeDAO.addAttribute(imeiType, subject, attributeIdx);
        }
        imeiAttribute.setStringValue(imei);

        AttributeEntity pinAttribute = this.attributeDAO.findAttribute(subject, pinType, attributeIdx);
        if (null == pinAttribute) {
            pinAttribute = this.attributeDAO.addAttribute(pinType, subject, attributeIdx);
        }
        pinAttribute.setStringValue(pin);

        AttributeEntity deviceDisableAttribute = this.attributeDAO.addAttribute(deviceDisableAttributeType, subject, attributeIdx);
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

    /**
     * {@inheritDoc}
     */
    public void remove(String userId, String imei, String pin) throws OptionAuthenticationException, OptionRegistrationException,
                                                              SubjectNotFoundException, AttributeTypeNotFoundException,
                                                              AttributeNotFoundException, DeviceDisabledException {

        String assignedSubject = authenticate(imei, pin);

        if (!assignedSubject.equals(userId))
            throw new OptionRegistrationException();

        SubjectEntity subject = this.subjectService.findSubject(userId);
        removeRegistration(subject, imei);
    }

    private void authenticate(SubjectEntity subject, String givenImei, String givenPin) throws OptionRegistrationException,
                                                                                       OptionAuthenticationException {

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

    private void removeRegistration(SubjectEntity subject, String imei) throws AttributeTypeNotFoundException {

        AttributeTypeEntity deviceAttributeType = this.attributeTypeDAO.getAttributeType(OptionConstants.OPTION_DEVICE_ATTRIBUTE);
        AttributeTypeEntity imeiAttributeType = this.attributeTypeDAO.getAttributeType(OptionConstants.IMEI_OPTION_ATTRIBUTE);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, deviceAttributeType);
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity imeiAttribute = this.attributeDAO
                                                             .findAttribute(subject, imeiAttributeType, deviceAttribute.getAttributeIndex());
            if (imeiAttribute.getStringValue().equals(imei)) {
                List<CompoundedAttributeTypeMemberEntity> members = deviceAttributeType.getMembers();
                for (CompoundedAttributeTypeMemberEntity member : members) {
                    AttributeEntity memberAttribute = this.attributeDAO.findAttribute(subject, member.getMember(),
                            deviceAttribute.getAttributeIndex());
                    if (null != memberAttribute) {
                        this.attributeDAO.removeAttribute(memberAttribute);
                    }
                }
                this.attributeDAO.removeAttribute(deviceAttribute);
                break;
            }
        }

        this.subjectIdentifierDAO.removeSubjectIdentifier(subject, OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String imei) throws DeviceNotFoundException, SubjectNotFoundException,
                                                   DeviceRegistrationNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(OptionConstants.OPTION_DEVICE_ID);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity imeiAttribute = this.attributeDAO.findAttribute(subject, OptionConstants.IMEI_OPTION_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (imeiAttribute.getStringValue().equals(imei)) {
                AttributeEntity disableAttribute = this.attributeDAO.findAttribute(subject, device.getDisableAttributeType(),
                        deviceAttribute.getAttributeIndex());
                disableAttribute.setBooleanValue(!disableAttribute.getBooleanValue());
                return;
            }
        }

        throw new DeviceRegistrationNotFoundException();

    }

}
