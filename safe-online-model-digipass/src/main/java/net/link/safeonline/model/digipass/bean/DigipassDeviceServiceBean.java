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

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.data.CompoundAttributeDO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
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

    private static final Log       LOG = LogFactory.getLog(DigipassDeviceServiceBean.class);

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService         subjectService;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO   subjectIdentifierDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO              deviceDAO;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger    securityAuditLogger;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        attributeManager = new AttributeManagerLWBean(attributeDAO, attributeTypeDAO);
    }

    public String authenticate(String userId, String token)
            throws SubjectNotFoundException, PermissionDeniedException, DeviceDisabledException {

        // FIXME: Authenticate doesn't provide the serialNumber?!
        // That means we can't properly find the right device to authenticate against.
        // And also means we can't properly figure out if that device is disabled.
        SubjectEntity subject = subjectService.getSubject(userId);

        try {
            DeviceEntity device = deviceDAO.getDevice(DigipassConstants.DIGIPASS_DEVICE_ID);
            List<AttributeEntity> attributes = attributeDAO.listAttributes(subject, device.getAttributeType());
            List<AttributeEntity> disableAttributes = attributeDAO.listAttributes(subject, device.getDisableAttributeType());

            if (attributes.isEmpty() || disableAttributes.isEmpty())
                return null;

            if (true == disableAttributes.get(0).getBooleanValue()) {
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, "device is disabled");
                throw new DeviceDisabledException();
            }

            return authenticate(subject, token);
        }

        catch (DeviceNotFoundException e) {
            throw new InternalInconsistencyException("Digipass device not defined.", e);
        }
    }

    private String authenticate(SubjectEntity subject, String token) {

        if (Integer.parseInt(token) % 2 != 0) {
            LOG.debug("Invalid token: " + token);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect digipass token");
            return null;
        }

        return subject.getUserId();
    }

    public String register(String userId, String serialNumber)
            throws SubjectNotFoundException, ArgumentIntegrityException {

        SubjectEntity existingMappedSubject = subjectIdentifierDAO.findSubject(DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber);
        if (null != existingMappedSubject)
            throw new ArgumentIntegrityException();

        SubjectEntity subject = subjectService.findSubject(userId);
        if (null == subject) {
            subject = subjectService.addSubjectWithoutLogin(userId);
        }
        setSerialNumber(subject, serialNumber);

        subjectIdentifierDAO.addSubjectIdentifier(DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber, subject);
        return userId;
    }

    private void setSerialNumber(SubjectEntity subject, String serialNumber) {

        try {
            CompoundAttributeDO deviceAttribute = attributeManager.newCompound(DigipassConstants.DIGIPASS_DEVICE_ATTRIBUTE, subject);
            deviceAttribute.addAttribute(DigipassConstants.DIGIPASS_SN_ATTRIBUTE, serialNumber);
            deviceAttribute.addAttribute(DigipassConstants.DIGIPASS_DEVICE_DISABLE_ATTRIBUTE, false);
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Digipass device attribute types not defined.", e);
        }
    }

    public void remove(String serialNumber)
            throws DigipassException {

        try {
            SubjectEntity subject = subjectIdentifierDAO.findSubject(DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber);
            if (null == subject)
                throw new DigipassException("device registration not found");

            attributeManager.removeCompoundWhere(subject, DigipassConstants.DIGIPASS_DEVICE_ATTRIBUTE,
                    DigipassConstants.DIGIPASS_SN_ATTRIBUTE, serialNumber);
            subjectIdentifierDAO.removeSubjectIdentifier(subject, DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber);
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Digipass device attribute types not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new InternalInconsistencyException("Tried to remove attributes that don't exist.", e);
        }
    }

    public List<AttributeDO> getDigipasses(String userId, Locale locale)
            throws SubjectNotFoundException {

        try {
            LOG.debug("get digipasses for: " + userId);
            DeviceEntity device = deviceDAO.getDevice(DigipassConstants.DIGIPASS_DEVICE_ID);
            SubjectEntity subject = subjectService.getSubject(userId);

            List<AttributeDO> attributes = new LinkedList<AttributeDO>();

            String humanReadableName = null;
            String description = null;
            AttributeTypeDescriptionEntity attributeTypeDescription = findAttributeTypeDescription(device.getUserAttributeType(), locale);
            if (null != attributeTypeDescription) {
                humanReadableName = attributeTypeDescription.getName();
                description = attributeTypeDescription.getDescription();
            }

            List<AttributeEntity> userAttributes = attributeDAO.listAttributes(subject, device.getUserAttributeType());
            for (AttributeEntity userAttribute : userAttributes) {
                attributes.add(new AttributeDO(device.getUserAttributeType().getName(), device.getUserAttributeType().getType(), true,
                        userAttribute.getAttributeIndex(), humanReadableName, description, false, false, userAttribute.getStringValue(),
                        false));
            }

            return attributes;
        }

        catch (DeviceNotFoundException e) {
            throw new InternalInconsistencyException("Digipass device not defined.", e);
        }
    }

    private AttributeTypeDescriptionEntity findAttributeTypeDescription(AttributeTypeEntity attributeType, Locale locale) {

        if (null == locale)
            return null;
        String language = locale.getLanguage();
        LOG.debug("getting description for digipass attributes in: " + language);

        return attributeTypeDAO.findDescription(new AttributeTypeDescriptionPK(attributeType.getName(), language));
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String serialNumber)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = subjectService.getSubject(userId);

        AttributeEntity disableAttribute = getDisableAttribute(serialNumber, subject);
        disableAttribute.setValue(true);
    }

    public String enable(String userId, String serialNumber, String token)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = subjectService.getSubject(userId);

        if (null == authenticate(subject, token))
            return null;

        AttributeEntity disableAttribute = getDisableAttribute(serialNumber, subject);
        disableAttribute.setValue(false);

        return subject.getUserId();
    }

    private AttributeEntity getDisableAttribute(String serialNumber, SubjectEntity subject)
            throws DeviceRegistrationNotFoundException {

        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, DigipassConstants.DIGIPASS_DEVICE_ATTRIBUTE,
                    DigipassConstants.DIGIPASS_SN_ATTRIBUTE, serialNumber);
            AttributeEntity disableAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    DigipassConstants.DIGIPASS_DEVICE_DISABLE_ATTRIBUTE);

            return disableAttribute;
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Digipass device attribute types not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException();
        }
    }
}
