/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.option.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

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
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.option.OptionConstants;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.service.SubjectService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link OptionDeviceServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Feb 4, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
@Stateless
@LocalBinding(jndiBinding = OptionDeviceService.JNDI_BINDING)
public class OptionDeviceServiceBean implements OptionDeviceService {

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService         subjectService;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO   subjectIdentifierDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO              deviceDAO;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        attributeManager = new AttributeManagerLWBean(attributeDAO);
    }

    /**
     * {@inheritDoc}
     */
    public String authenticate(String imei)
            throws SubjectNotFoundException, DeviceNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException {

        // check registration exists
        SubjectEntity subject = subjectIdentifierDAO.findSubject(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
        if (null == subject)
            throw new SubjectNotFoundException();

        if (findDisableAttribute(imei, subject.getUserId()).getBooleanValue())
            throw new DeviceDisabledException();

        return subject.getUserId();
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String imei, String userId)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException {

        findDisableAttribute(imei, userId).setValue(true);
    }

    /**
     * {@inheritDoc}
     */
    public void enable(String imei, String userId)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException {

        findDisableAttribute(imei, userId).setValue(false);
    }

    public AttributeEntity findDisableAttribute(String imei, String userId)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(OptionConstants.OPTION_DEVICE_ID);
        SubjectEntity subject = subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity imeiAttribute = attributeDAO.findAttribute(subject, OptionConstants.OPTION_IMEI_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (imeiAttribute.getStringValue().equals(imei))
                return attributeDAO.findAttribute(subject, device.getDisableAttributeType(), deviceAttribute.getAttributeIndex());
        }

        throw new DeviceRegistrationNotFoundException();
    }

    /**
     * {@inheritDoc}
     */
    public void register(String imei, String userId)
            throws AttributeTypeNotFoundException {

        SubjectEntity subject = subjectService.findSubject(userId);
        if (null == subject) {
            subject = subjectService.addSubjectWithoutLogin(userId);
        }

        setAttributes(subject, imei);
        subjectIdentifierDAO.addSubjectIdentifier(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei, subject);
    }

    private void setAttributes(SubjectEntity subject, String imei)
            throws AttributeTypeNotFoundException {

        AttributeTypeEntity deviceAttributeType = attributeTypeDAO.getAttributeType(OptionConstants.OPTION_DEVICE_ATTRIBUTE);
        AttributeTypeEntity imeiAttributeType = attributeTypeDAO.getAttributeType(OptionConstants.OPTION_IMEI_ATTRIBUTE);
        AttributeTypeEntity deviceDisableAttributeType = attributeTypeDAO.getAttributeType(OptionConstants.OPTION_DEVICE_DISABLE_ATTRIBUTE);

        AttributeEntity imeiAttribute = attributeDAO.addAttribute(imeiAttributeType, subject);
        imeiAttribute.setStringValue(imei);
        AttributeEntity deviceDisableAttribute = attributeDAO.addAttribute(deviceDisableAttributeType, subject);
        deviceDisableAttribute.setBooleanValue(false);

        AttributeEntity deviceAttribute = attributeDAO.addAttribute(deviceAttributeType, subject);
        deviceAttribute.setStringValue(UUID.randomUUID().toString());
        List<AttributeEntity> deviceAttributeMembers = new LinkedList<AttributeEntity>();
        deviceAttributeMembers.add(imeiAttribute);
        deviceAttributeMembers.add(deviceDisableAttribute);
        deviceAttribute.setMembers(deviceAttributeMembers);
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String imei)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException {

        // check registration exists
        SubjectEntity subject = subjectIdentifierDAO.findSubject(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
        if (null == subject)
            throw new SubjectNotFoundException();

        AttributeTypeEntity deviceAttributeType = attributeTypeDAO.getAttributeType(OptionConstants.OPTION_DEVICE_ATTRIBUTE);
        AttributeTypeEntity imeiAttributeType = attributeTypeDAO.getAttributeType(OptionConstants.OPTION_IMEI_ATTRIBUTE);

        List<AttributeEntity> deviceAttributes = attributeDAO.listAttributes(subject, deviceAttributeType);
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity imeiAttribute = attributeDAO.findAttribute(subject, imeiAttributeType, deviceAttribute.getAttributeIndex());
            if (imeiAttribute.getStringValue().equals(imei)) {
                attributeManager.removeAttribute(deviceAttributeType, deviceAttribute.getAttributeIndex(), subject);
                break;
            }
        }

        subjectIdentifierDAO.removeSubjectIdentifier(subject, OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
    }
}
