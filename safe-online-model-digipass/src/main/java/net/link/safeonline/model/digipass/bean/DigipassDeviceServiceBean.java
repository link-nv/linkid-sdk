/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.digipass.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
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
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.digipass.DigipassConstants;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.model.digipass.DigipassDeviceServiceRemote;
import net.link.safeonline.model.digipass.DigipassException;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = DigipassDeviceService.JNDI_BINDING)
public class DigipassDeviceServiceBean implements DigipassDeviceService, DigipassDeviceServiceRemote {

    private static final Log     LOG = LogFactory.getLog(DigipassDeviceServiceBean.class);

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService       subjectService;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO subjectIdentifierDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO            deviceDAO;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO         attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO     attributeTypeDAO;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger  securityAuditLogger;


    public String authenticate(String userId, String token)
            throws SubjectNotFoundException, PermissionDeniedException, DeviceNotFoundException, DeviceDisabledException {

        SubjectEntity subject = this.subjectService.getSubject(userId);
        DeviceEntity device = this.deviceDAO.getDevice(DigipassConstants.DIGIPASS_DEVICE_ID);
        List<AttributeEntity> attributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        List<AttributeEntity> disableAttributes = this.attributeDAO.listAttributes(subject, device.getDisableAttributeType());

        if (0 == attributes.size())
            return null;
        if (true == disableAttributes.get(0).getBooleanValue()) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, "device is disabled");
            throw new DeviceDisabledException();
        }
        if (Integer.parseInt(token) % 2 != 0) {
            LOG.debug("Invalid token: " + token);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, "incorrect digipass token");
            return null;
        }
        return userId;
    }

    public String register(String userId, String serialNumber)
            throws SubjectNotFoundException, PermissionDeniedException, ArgumentIntegrityException, AttributeTypeNotFoundException {

        SubjectEntity existingMappedSubject = this.subjectIdentifierDAO.findSubject(DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN,
                serialNumber);
        if (null != existingMappedSubject)
            throw new ArgumentIntegrityException();

        SubjectEntity subject = this.subjectService.findSubject(userId);
        if (null == subject) {
            subject = this.subjectService.addSubjectWithoutLogin(userId);
        }
        setSerialNumber(subject, serialNumber);

        this.subjectIdentifierDAO.addSubjectIdentifier(DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber, subject);
        return userId;
    }

    private void setSerialNumber(SubjectEntity subject, String serialNumber)
            throws AttributeTypeNotFoundException {

        AttributeTypeEntity deviceAttributeType = this.attributeTypeDAO.getAttributeType(DigipassConstants.DIGIPASS_DEVICE_ATTRIBUTE);
        AttributeTypeEntity snAttributeType = this.attributeTypeDAO.getAttributeType(DigipassConstants.DIGIPASS_SN_ATTRIBUTE);
        AttributeTypeEntity deviceDisableAttributeType = this.attributeTypeDAO
                                                                              .getAttributeType(DigipassConstants.DIGIPASS_DEVICE_DISABLE_ATTRIBUTE);

        int attributeIdx = this.attributeDAO.listAttributes(subject, deviceAttributeType).size();

        AttributeEntity snAttribute = this.attributeDAO.addAttribute(snAttributeType, subject, attributeIdx);
        snAttribute.setStringValue(serialNumber);
        AttributeEntity deviceDisableAttribute = this.attributeDAO.addAttribute(deviceDisableAttributeType, subject, attributeIdx);
        deviceDisableAttribute.setBooleanValue(false);

        AttributeEntity deviceAttribute = this.attributeDAO.addAttribute(deviceAttributeType, subject);
        deviceAttribute.setStringValue(UUID.randomUUID().toString());
        List<AttributeEntity> deviceAttributeMembers = new LinkedList<AttributeEntity>();
        deviceAttributeMembers.add(snAttribute);
        deviceAttributeMembers.add(deviceDisableAttribute);
        deviceAttribute.setMembers(deviceAttributeMembers);
    }

    public void remove(String serialNumber)
            throws DigipassException, AttributeTypeNotFoundException {

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber);
        if (null == subject)
            throw new DigipassException("device registration not found");

        AttributeTypeEntity deviceAttributeType = this.attributeTypeDAO.getAttributeType(DigipassConstants.DIGIPASS_DEVICE_ATTRIBUTE);
        AttributeTypeEntity snAttributeType = this.attributeTypeDAO.getAttributeType(DigipassConstants.DIGIPASS_SN_ATTRIBUTE);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, deviceAttributeType);
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = this.attributeDAO
                                                               .findAttribute(subject, snAttributeType, deviceAttribute.getAttributeIndex());
            if (mobileAttribute.getStringValue().equals(serialNumber)) {
                LOG.debug("remove attribute");
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

        this.subjectIdentifierDAO.removeSubjectIdentifier(subject, DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber);
    }

    public List<AttributeDO> getDigipasses(String userId, Locale locale)
            throws SubjectNotFoundException, PermissionDeniedException, DeviceNotFoundException {

        LOG.debug("get digipasses for: " + userId);
        DeviceEntity device = this.deviceDAO.getDevice(DigipassConstants.DIGIPASS_DEVICE_ID);
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
    public void disable(String userId, String serialNumber)
            throws SubjectNotFoundException, DeviceNotFoundException, DeviceRegistrationNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(DigipassConstants.DIGIPASS_DEVICE_ID);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity snAttribute = this.attributeDAO.findAttribute(subject, DigipassConstants.DIGIPASS_SN_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (snAttribute.getStringValue().equals(serialNumber)) {
                LOG.debug("disable digipass " + serialNumber);
                AttributeEntity disableAttribute = this.attributeDAO.findAttribute(subject, device.getDisableAttributeType(),
                        deviceAttribute.getAttributeIndex());
                disableAttribute.setBooleanValue(!disableAttribute.getBooleanValue());
                return;
            }
        }

        throw new DeviceRegistrationNotFoundException();

    }

}
