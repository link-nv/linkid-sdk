/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.beid.bean;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.security.auth.x500.X500Principal;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.IdentityStatementAttributes;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = BeIdPkiProviderBean.JNDI_BINDING)
public class BeIdPkiProviderBean implements PkiProvider {

    public static final String     JNDI_BINDING           = PkiProvider.JNDI_PREFIX + "beid";

    public static final String     TRUST_DOMAIN_NAME      = "beid";

    public static final String     IDENTIFIER_DOMAIN_NAME = "beid";

    private static final Log       LOG                    = LogFactory.getLog(BeIdPkiProviderBean.class);

    @Resource
    private SessionContext         context;

    @EJB(mappedName = TrustDomainDAO.JNDI_BINDING)
    private TrustDomainDAO         trustDomainDAO;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO              deviceDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService         subjectService;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO   subjectIdentifierDAO;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        this.attributeManager = new AttributeManagerLWBean(this.attributeDAO);
    }

    public boolean accept(X509Certificate certificate) {

        X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
        String subject = subjectPrincipal.toString();
        LOG.debug("subject: " + subject);
        if (subject.indexOf("SERIALNUMBER") == -1)
            return false;
        if (subject.indexOf("GIVENNAME") == -1)
            return false;
        if (subject.indexOf("SURNAME") == -1)
            return false;
        return true;
    }

    public TrustDomainEntity getTrustDomain()
            throws TrustDomainNotFoundException {

        TrustDomainEntity trustDomain = this.trustDomainDAO.getTrustDomain(TRUST_DOMAIN_NAME);
        return trustDomain;
    }

    public PkiProvider getReference() {

        LOG.debug("get reference");
        PkiProvider reference = this.context.getBusinessObject(PkiProvider.class);
        return reference;
    }

    public String mapAttribute(IdentityStatementAttributes identityStatementAttributes) {

        switch (identityStatementAttributes) {
            case SURNAME:
                return BeIdConstants.SURNAME_ATTRIBUTE;
            case GIVEN_NAME:
                return BeIdConstants.GIVENNAME_ATTRIBUTE;
            case AUTH_CERT:
                return BeIdConstants.AUTH_CERT_ATTRIBUTE;
            default:
                throw new IllegalArgumentException("unsupported identity statement attribute");
        }
    }

    public String getIdentifierDomainName() {

        return IDENTIFIER_DOMAIN_NAME;
    }

    public String getSubjectIdentifier(X509Certificate certificate) {

        byte[] data;
        try {
            data = certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new IllegalArgumentException("cert encoding error: " + e.getMessage());
        }
        String identifier = DigestUtils.shaHex(data);
        return identifier;
    }

    public void storeAdditionalAttributes(SubjectEntity subject, X509Certificate certificate) {

        String subjectName = getSubjectName(certificate);
        String nrn = getAttributeFromSubjectName(subjectName, "SERIALNUMBER");
        setAttribute(BeIdConstants.NRN_ATTRIBUTE, subject, nrn);

        // Identifier attribute used for removal, no authentication is required in that step so no access to the eID certificate.
        String identifier = getSubjectIdentifier(certificate);
        setAttribute(BeIdConstants.IDENTIFIER_ATTRIBUTE, subject, identifier);
    }

    private void setAttribute(String attributeName, SubjectEntity subject, String value) {

        AttributeTypeEntity attributeType;
        try {
            attributeType = this.attributeTypeDAO.getAttributeType(attributeName);
        } catch (AttributeTypeNotFoundException e) {
            throw new EJBException("attribute type not found");
        }
        AttributeEntity attribute = this.attributeDAO.addAttribute(attributeType, subject);
        attribute.setStringValue(value);
    }

    private String getSubjectName(X509Certificate certificate) {

        X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
        String subjectName = subjectPrincipal.toString();
        return subjectName;
    }

    private String getAttributeFromSubjectName(String subjectName, String attributeName) {

        int attributeBegin = subjectName.indexOf(attributeName + "=");
        if (-1 == attributeBegin)
            throw new IllegalArgumentException("attribute name does not occur in subject: " + attributeName);
        attributeBegin += attributeName.length() + 1; // "attributeName="
        int attributeEnd = subjectName.indexOf(",", attributeBegin);
        if (-1 == attributeEnd) {
            // last field has no trailing ","
            attributeEnd = subjectName.length();
        }
        String attributeValue = subjectName.substring(attributeBegin, attributeEnd);
        return attributeValue;
    }

    public void storeDeviceAttribute(SubjectEntity subject, long index)
            throws DeviceNotFoundException, AttributeNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(BeIdConstants.BEID_DEVICE_ID);
        AttributeTypeEntity deviceAttributeType = device.getAttributeType();
        AttributeTypeEntity deviceUserAttributeType = device.getUserAttributeType();
        AttributeTypeEntity deviceDisableAttributeType = device.getDisableAttributeType();

        AttributeEntity givenNameAttribute = this.attributeDAO.getAttribute(BeIdConstants.GIVENNAME_ATTRIBUTE, subject, index);
        AttributeEntity surNameAttribute = this.attributeDAO.getAttribute(BeIdConstants.SURNAME_ATTRIBUTE, subject, index);
        AttributeEntity deviceUserAttribute = this.attributeDAO.findAttribute(subject, deviceUserAttributeType, index);
        if (null == deviceUserAttribute) {
            String userAttributeValue = getUserAttributeValue(givenNameAttribute, surNameAttribute);
            deviceUserAttribute = this.attributeDAO.addAttribute(deviceUserAttributeType, subject, index);
            deviceUserAttribute.setStringValue(userAttributeValue);
        }
        AttributeEntity deviceDisableAttribute = this.attributeDAO.findAttribute(subject, deviceDisableAttributeType, index);
        if (null == deviceDisableAttribute) {
            deviceDisableAttribute = this.attributeDAO.addAttribute(deviceDisableAttributeType, subject, index);
            deviceDisableAttribute.setBooleanValue(false);
        }

        AttributeEntity deviceAttribute = this.attributeDAO.findAttribute(subject, deviceAttributeType, index);
        if (null == deviceAttribute) {
            deviceAttribute = this.attributeDAO.addAttribute(deviceAttributeType, subject, index);
            deviceAttribute.setStringValue(UUID.randomUUID().toString());
            List<AttributeEntity> deviceAttributeMembers = new LinkedList<AttributeEntity>();
            deviceAttributeMembers.add(givenNameAttribute);
            deviceAttributeMembers.add(surNameAttribute);
            deviceAttributeMembers.add(this.attributeDAO.getAttribute(BeIdConstants.NRN_ATTRIBUTE, subject, index));
            deviceAttributeMembers.add(this.attributeDAO.getAttribute(BeIdConstants.IDENTIFIER_ATTRIBUTE, subject, index));
            deviceAttributeMembers.add(deviceUserAttribute);
            deviceAttributeMembers.add(deviceDisableAttribute);
            deviceAttribute.setMembers(deviceAttributeMembers);
        }
    }

    public boolean isDisabled(SubjectEntity subject, X509Certificate certificate)
            throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(BeIdConstants.BEID_DEVICE_ID);
        String subjectName = getSubjectName(certificate);
        String nrn = getAttributeFromSubjectName(subjectName, "SERIALNUMBER");
        AttributeTypeEntity deviceAttributeType = device.getAttributeType();
        List<AttributeEntity> attributes = this.attributeDAO.listAttributes(subject, deviceAttributeType);
        LOG.debug("check if device is enabled (nrn=" + nrn + ")");
        for (AttributeEntity attribute : attributes) {
            AttributeEntity nrnAttribute = this.attributeDAO.findAttribute(subject, BeIdConstants.NRN_ATTRIBUTE,
                    attribute.getAttributeIndex());
            if (nrnAttribute.getStringValue().equals(nrn)) {
                AttributeEntity disabledAttribute = this.attributeDAO.findAttribute(subject, BeIdConstants.BEID_DEVICE_DISABLE_ATTRIBUTE,
                        attribute.getAttributeIndex());
                return disabledAttribute.getBooleanValue();
            }
        }
        return false;
    }

    public List<AttributeEntity> listDeviceAttributes(SubjectEntity subject)
            throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(BeIdConstants.BEID_DEVICE_ID);
        return this.attributeDAO.listAttributes(subject, device.getAttributeType());
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String attribute)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(BeIdConstants.BEID_DEVICE_ID);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity givenNameAttribute = this.attributeDAO.findAttribute(subject, BeIdConstants.GIVENNAME_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            AttributeEntity surNameAttribute = this.attributeDAO.findAttribute(subject, BeIdConstants.SURNAME_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            String deviceUserAttribute = getUserAttributeValue(givenNameAttribute, surNameAttribute);
            if (deviceUserAttribute.equals(attribute)) {
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
    public void enable(SubjectEntity subject, X509Certificate certificate)
            throws DeviceNotFoundException, PermissionDeniedException, AttributeNotFoundException, AttributeTypeNotFoundException,
            DeviceRegistrationNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(BeIdConstants.BEID_DEVICE_ID);
        String subjectName = getSubjectName(certificate);
        String nrn = getAttributeFromSubjectName(subjectName, "SERIALNUMBER");
        AttributeTypeEntity deviceAttributeType = device.getAttributeType();
        List<AttributeEntity> attributes = this.attributeDAO.listAttributes(subject, deviceAttributeType);
        LOG.debug("enable device registration: nrn=" + nrn);
        for (AttributeEntity attribute : attributes) {
            AttributeEntity nrnAttribute = this.attributeDAO.findAttribute(subject, BeIdConstants.NRN_ATTRIBUTE,
                    attribute.getAttributeIndex());
            if (nrnAttribute.getStringValue().equals(nrn)) {
                AttributeEntity disableAttribute = this.attributeDAO.findAttribute(subject, device.getDisableAttributeType(),
                        attribute.getAttributeIndex());
                disableAttribute.setBooleanValue(false);
                return;
            }
        }

        throw new DeviceRegistrationNotFoundException();
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String userId, String attribute)
            throws SubjectNotFoundException, DeviceNotFoundException, AttributeTypeNotFoundException, DeviceRegistrationNotFoundException,
            AttributeNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(BeIdConstants.BEID_DEVICE_ID);
        SubjectEntity subject = this.subjectService.getSubject(userId);

        List<AttributeEntity> deviceAttributes = this.attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity givenNameAttribute = this.attributeDAO.findAttribute(subject, BeIdConstants.GIVENNAME_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            AttributeEntity surNameAttribute = this.attributeDAO.findAttribute(subject, BeIdConstants.SURNAME_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            String deviceUserAttribute = getUserAttributeValue(givenNameAttribute, surNameAttribute);
            if (deviceUserAttribute.equals(attribute)) {
                AttributeTypeEntity identifierAttributeType = this.attributeTypeDAO.getAttributeType(BeIdConstants.IDENTIFIER_ATTRIBUTE);
                AttributeEntity identifierAttribute = this.attributeDAO.findAttribute(subject, identifierAttributeType,
                        deviceAttribute.getAttributeIndex());
                this.subjectIdentifierDAO.removeSubjectIdentifier(subject, getIdentifierDomainName(), identifierAttribute.getStringValue());
                this.attributeManager.removeAttribute(device.getAttributeType(), deviceAttribute.getAttributeIndex(), subject);
                return;
            }
        }

        throw new DeviceRegistrationNotFoundException();

    }

    private String getUserAttributeValue(AttributeEntity givenNameAttribute, AttributeEntity surNameAttribute) {

        return surNameAttribute.getStringValue() + ", " + givenNameAttribute.getStringValue();
    }

}
