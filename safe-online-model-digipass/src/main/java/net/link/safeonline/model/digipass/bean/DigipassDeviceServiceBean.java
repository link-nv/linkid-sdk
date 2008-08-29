/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.digipass.bean;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.link.safeonline.audit.ResourceAuditLogger;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
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
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.digipass.DigipassConstants;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.model.digipass.DigipassDeviceServiceRemote;
import net.link.safeonline.model.digipass.DigipassException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class DigipassDeviceServiceBean implements DigipassDeviceService, DigipassDeviceServiceRemote {

    private static final Log     LOG = LogFactory.getLog(DigipassDeviceServiceBean.class);

    @EJB
    private SubjectService       subjectService;

    @EJB
    private SubjectIdentifierDAO subjectIdentifierDAO;

    @EJB
    private DeviceDAO            deviceDAO;

    @EJB
    private AttributeDAO         attributeDAO;

    @EJB
    private AttributeTypeDAO     attributeTypeDAO;

    @EJB
    private SecurityAuditLogger  securityAuditLogger;

    @EJB
    private ResourceAuditLogger  resourceAuditLogger;


    public String authenticate(String loginName, String token) throws SubjectNotFoundException,
            PermissionDeniedException, DeviceNotFoundException {

        NameIdentifierMappingClient idMappingClient = getIDMappingClient();
        String userId;
        try {
            userId = idMappingClient.getUserId(loginName);
        } catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
            LOG.debug("subject not found: " + loginName);
            throw new SubjectNotFoundException();
        } catch (RequestDeniedException e) {
            LOG.debug("request denied: " + e.getMessage());
            throw new PermissionDeniedException("Unable to retrieve login: " + loginName);
        } catch (WSClientTransportException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE, e
                    .getLocation(), "Unable to contact id mapping WS");
            throw new PermissionDeniedException(e.getMessage());
        }
        SubjectEntity subject = this.subjectService.getSubject(userId);
        DeviceEntity device = this.deviceDAO.getDevice(DigipassConstants.DIGIPASS_DEVICE_ID);
        List<AttributeEntity> attributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());

        if (0 == attributes.size())
            return null;
        if (Integer.parseInt(token) % 2 != 0) {
            LOG.debug("Invalid token: " + token);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, "incorrect digipass token");
            return null;
        }
        return userId;
    }

    public String register(String loginName, String serialNumber) throws SubjectNotFoundException,
            PermissionDeniedException, ArgumentIntegrityException {

        NameIdentifierMappingClient idMappingClient = getIDMappingClient();
        String userId;
        try {
            userId = idMappingClient.getUserId(loginName);
        } catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
            LOG.debug("subject not found: " + loginName);
            throw new SubjectNotFoundException();
        } catch (RequestDeniedException e) {
            LOG.debug("request denied: " + e.getMessage());
            throw new PermissionDeniedException("Unable to retrieve login: " + loginName);
        } catch (WSClientTransportException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE, e
                    .getLocation(), "Unable to contact id mapping WS");
            throw new PermissionDeniedException(e.getMessage());
        }

        SubjectEntity existingMappedSubject = this.subjectIdentifierDAO.findSubject(
                DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber);
        if (null != existingMappedSubject) {
            throw new ArgumentIntegrityException();
        }

        SubjectEntity subject = this.subjectService.findSubject(userId);
        if (null == subject) {
            subject = this.subjectService.addSubjectWithoutLogin(userId);
        }
        setSerialNumber(subject, serialNumber);

        this.subjectIdentifierDAO.addSubjectIdentifier(DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber,
                subject);
        return userId;
    }

    private void setSerialNumber(SubjectEntity subject, String serialNumber) {

        AttributeTypeEntity snAttributeType;
        try {
            snAttributeType = this.attributeTypeDAO.getAttributeType(DigipassConstants.DIGIPASS_SN_ATTRIBUTE);
        } catch (AttributeTypeNotFoundException e) {
            throw new EJBException("digipass serial number attribute type not found");
        }
        List<AttributeEntity> mobileAttributes = this.attributeDAO.listAttributes(subject, snAttributeType);
        AttributeEntity snAttribute = this.attributeDAO.addAttribute(snAttributeType, subject, mobileAttributes.size());
        snAttribute.setStringValue(serialNumber);
    }

    public void remove(String loginName, String serialNumber) throws SubjectNotFoundException, DigipassException,
            PermissionDeniedException, DeviceNotFoundException {

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN,
                serialNumber);
        if (null == subject)
            throw new DigipassException("device registration not found");
        this.subjectIdentifierDAO.removeSubjectIdentifier(subject, DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN,
                serialNumber);

        DeviceEntity device = this.deviceDAO.getDevice(DigipassConstants.DIGIPASS_DEVICE_ID);
        AttributeTypeEntity deviceAttributeType = device.getAttributeType();
        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, deviceAttributeType);
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            if (deviceAttribute.getStringValue().equals(serialNumber)) {
                this.attributeDAO.removeAttribute(deviceAttribute);
            }
        }
    }

    public List<AttributeDO> getDigipasses(String loginName, Locale locale) throws SubjectNotFoundException,
            PermissionDeniedException, DeviceNotFoundException {

        LOG.debug("get digipasses for: " + loginName);
        NameIdentifierMappingClient idMappingClient = getIDMappingClient();
        String userId;
        try {
            userId = idMappingClient.getUserId(loginName);
        } catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
            LOG.debug("subject not found: " + loginName);
            throw new SubjectNotFoundException();
        } catch (RequestDeniedException e) {
            LOG.debug("request denied: " + e.getMessage());
            throw new PermissionDeniedException("Unable to retrieve login: " + loginName);
        } catch (WSClientTransportException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE, e
                    .getLocation(), "Unable to contact id mapping WS");
            throw new PermissionDeniedException(e.getMessage());
        }

        DeviceEntity device = this.deviceDAO.getDevice(DigipassConstants.DIGIPASS_DEVICE_ID);
        AttributeTypeDescriptionEntity attributeTypeDescription = findAttributeTypeDescription(device
                .getAttributeType(), locale);
        String humanReadableName = null;
        String description = null;
        if (null != attributeTypeDescription) {
            humanReadableName = attributeTypeDescription.getName();
            description = attributeTypeDescription.getDescription();
        }

        SubjectEntity subject = this.subjectService.getSubject(userId);
        List<AttributeDO> deviceAttributeList = new LinkedList<AttributeDO>();
        List<AttributeEntity> attributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity attribute : attributes) {
            AttributeDO attributeView = new AttributeDO(device.getAttributeType().getName(), device.getAttributeType()
                    .getType(), device.getAttributeType().isMultivalued(), attribute.getAttributeIndex(),
                    humanReadableName, description, device.getAttributeType().isUserEditable(), false, attribute
                            .getStringValue(), attribute.getBooleanValue());
            deviceAttributeList.add(attributeView);
        }
        return deviceAttributeList;
    }

    private AttributeTypeDescriptionEntity findAttributeTypeDescription(AttributeTypeEntity attributeType, Locale locale) {

        String language;
        if (null == locale) {
            language = null;
        } else {
            language = locale.getLanguage();
        }
        if (null != language) {
            AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
                    .findDescription(new AttributeTypeDescriptionPK(attributeType.getName(), language));
            return attributeTypeDescription;
        }
        return null;
    }

    private NameIdentifierMappingClient getIDMappingClient() {

        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        String location = externalContext.getInitParameter("StsWsLocation");

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        PrivateKey privateKey = authIdentityServiceClient.getPrivateKey();
        X509Certificate certificate = authIdentityServiceClient.getCertificate();

        NameIdentifierMappingClient client = new NameIdentifierMappingClientImpl(location, certificate, privateKey);
        return client;
    }
}
