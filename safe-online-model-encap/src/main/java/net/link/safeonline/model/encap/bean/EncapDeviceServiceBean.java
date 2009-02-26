/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.encap.bean;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.data.CompoundAttributeDO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.model.encap.EncapDeviceServiceRemote;
import net.link.safeonline.model.encap.MobileManager;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateful
@LocalBinding(jndiBinding = EncapDeviceService.JNDI_BINDING)
public class EncapDeviceServiceBean implements EncapDeviceService, EncapDeviceServiceRemote {

    private static final Log       LOG = LogFactory.getLog(EncapDeviceServiceBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager          entityManager;

    @EJB(mappedName = HistoryDAO.JNDI_BINDING)
    private HistoryDAO             historyDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService         subjectService;

    @EJB(mappedName = NodeMappingService.JNDI_BINDING)
    private NodeMappingService     nodeMappingService;

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

    private String                 challengeMobile;
    private String                 challengeCode;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        attributeManager = new AttributeManagerLWBean(attributeDAO, attributeTypeDAO);
    }

    private AttributeEntity getDisableAttribute(SubjectEntity subject, String mobile)
            throws DeviceRegistrationNotFoundException {

        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, EncapConstants.ENCAP_DEVICE_ATTRIBUTE,
                    EncapConstants.ENCAP_MOBILE_ATTRIBUTE, mobile);
            AttributeEntity disableAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    EncapConstants.ENCAP_DEVICE_DISABLE_ATTRIBUTE);

            return disableAttribute;
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for Encap device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String authenticate(String mobileOTP)
            throws SubjectNotFoundException, DeviceDisabledException, DeviceRegistrationNotFoundException, MobileException,
            DeviceAuthenticationException {

        if (!isChallenged())
            return null;

        SubjectEntity subject = subjectIdentifierDAO.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, challengeMobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        if (true == getDisableAttribute(subject, challengeMobile).getBooleanValue())
            throw new DeviceDisabledException();

        if (false == mobileManager.verifyOTP(challengeCode, mobileOTP)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(), "incorrect mobile token");
            throw new DeviceAuthenticationException();
        }

        // Success, reset the challenge data.
        challengeMobile = null;
        challengeCode = null;

        return subject.getUserId();
    }

    /**
     * {@inheritDoc}
     */
    public String register(String mobile)
            throws MobileException, DeviceRegistrationException {

        String activationCode = mobileManager.activate(mobile, null);
        if (null == activationCode)
            throw new DeviceRegistrationException();

        return activationCode;
    }

    /**
     * {@inheritDoc}
     */
    public void commitRegistration(String nodeName, String userId, String mobileOTP)
            throws MobileException, DeviceAuthenticationException, NodeNotFoundException {

        if (false == mobileManager.verifyOTP(challengeCode, mobileOTP)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, "incorrect mobile token");
            throw new DeviceAuthenticationException();
        }

        // Remove any old subjects that use this mobile.
        SubjectEntity subject = subjectIdentifierDAO.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, challengeMobile);
        if (null != subject) {
            mobileManager.remove(challengeMobile);
            // flush and clear to commit and release the removed entities.
            entityManager.flush();
            entityManager.clear();
        }

        /*
         * Check through node mapping if subject exists, if not, it is created.
         */
        subject = nodeMappingService.getSubject(userId, nodeName);

        // Create the device attributes.
        try {
            CompoundAttributeDO deviceAttribute = attributeManager.newCompound(EncapConstants.ENCAP_DEVICE_ATTRIBUTE, subject);
            deviceAttribute.addAttribute(EncapConstants.ENCAP_MOBILE_ATTRIBUTE, challengeMobile);
            deviceAttribute.addAttribute(EncapConstants.ENCAP_DEVICE_DISABLE_ATTRIBUTE, false);

            // Add the subject mapping.
            subjectIdentifierDAO.addSubjectIdentifier(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, challengeMobile, subject);

            historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                    SafeOnlineConstants.DEVICE_PROPERTY, EncapConstants.ENCAP_DEVICE_ID));
            // Success, reset the challenge data.
            challengeMobile = null;
            challengeCode = null;

        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for Encap device not defined.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, MobileException {

        mobileManager.remove(mobile);

        SubjectEntity subject = subjectIdentifierDAO.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);
        if (null == subject)
            throw new SubjectNotFoundException();

        try {
            attributeManager.removeCompoundWhere(subject, EncapConstants.ENCAP_DEVICE_ATTRIBUTE, EncapConstants.ENCAP_MOBILE_ATTRIBUTE,
                    mobile);
            subjectIdentifierDAO.removeSubjectIdentifier(subject, EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);

            // flush and clear to commit and release the removed entities.
            entityManager.flush();
            entityManager.clear();

            historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REMOVAL, Collections.singletonMap(
                    SafeOnlineConstants.DEVICE_PROPERTY, EncapConstants.ENCAP_DEVICE_ID));
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for Encap device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void requestOTP(String mobile)
            throws MobileException {

        challengeMobile = mobile;
        challengeCode = mobileManager.requestOTP(challengeMobile);
    }

    /**
     * {@inheritDoc}
     */
    public List<AttributeDO> getMobiles(String userId, Locale locale)
            throws SubjectNotFoundException {

        try {
            DeviceEntity device = deviceDAO.getDevice(EncapConstants.ENCAP_DEVICE_ID);
            SubjectEntity subject = subjectService.getSubject(userId);

            String humanReadableName = null;
            String description = null;
            AttributeTypeDescriptionEntity attributeTypeDescription = findAttributeTypeDescription(device.getUserAttributeType(), locale);
            if (null != attributeTypeDescription) {
                humanReadableName = attributeTypeDescription.getName();
                description = attributeTypeDescription.getDescription();
            }

            List<AttributeDO> attributes = new LinkedList<AttributeDO>();
            for (AttributeEntity userAttribute : attributeDAO.listAttributes(subject, device.getUserAttributeType())) {
                attributes.add(new AttributeDO(device.getUserAttributeType().getName(), device.getUserAttributeType().getType(), true,
                        userAttribute.getAttributeIndex(), humanReadableName, description, false, false, userAttribute.getStringValue(),
                        false));
            }

            return attributes;
        }

        catch (DeviceNotFoundException e) {
            throw new InternalInconsistencyException("Attributes for Encap device not defined.");
        }
    }

    private AttributeTypeDescriptionEntity findAttributeTypeDescription(AttributeTypeEntity attributeType, Locale locale) {

        if (null == locale)
            return null;

        String language = locale.getLanguage();
        LOG.debug("trying language: " + language);
        AttributeTypeDescriptionEntity attributeTypeDescription = attributeTypeDAO.findDescription(new AttributeTypeDescriptionPK(
                attributeType.getName(), language));

        return attributeTypeDescription;
    }

    /**
     * {@inheritDoc}
     */
    public void enable(String userId, String mobileOTP)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, MobileException, DeviceAuthenticationException {

        if (false == mobileManager.verifyOTP(challengeCode, mobileOTP)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, "incorrect mobile token");
            throw new DeviceAuthenticationException();
        }

        SubjectEntity subject = subjectService.getSubject(userId);
        AttributeEntity disableAttribute = getDisableAttribute(subject, challengeMobile);
        disableAttribute.setValue(false);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_ENABLE, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                EncapConstants.ENCAP_DEVICE_ID));

        // Success, reset the challenge data.
        challengeMobile = null;
        challengeCode = null;
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = subjectService.getSubject(userId);
        AttributeEntity disableAttribute = getDisableAttribute(subject, mobile);
        disableAttribute.setValue(true);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_DISABLE, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                EncapConstants.ENCAP_DEVICE_ID));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isChallenged() {

        return challengeMobile != null && challengeCode != null;
    }
}
