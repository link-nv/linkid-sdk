/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.beid.bean;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.PostActivate;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.data.CompoundAttributeDO;
import net.link.safeonline.entity.AttributeEntity;
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

    public static final String               JNDI_BINDING           = PkiProvider.JNDI_PREFIX + "beid";

    public static final String               TRUST_DOMAIN_NAME      = "beid";

    public static final String               IDENTIFIER_DOMAIN_NAME = "beid";

    private static final Log                 LOG                    = LogFactory.getLog(BeIdPkiProviderBean.class);

    @Resource
    private SessionContext                   context;

    @EJB(mappedName = TrustDomainDAO.JNDI_BINDING)
    private TrustDomainDAO                   trustDomainDAO;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO                     attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO                 attributeTypeDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService                   subjectService;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO             subjectIdentifierDAO;

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

    public boolean accept(X509Certificate certificate) {

        String subjectDN = certificate.getSubjectX500Principal().toString();
        LOG.debug("subject: " + subjectDN);

        if (subjectDN.indexOf("SERIALNUMBER") == -1)
            return false;
        if (subjectDN.indexOf("GIVENNAME") == -1)
            return false;
        if (subjectDN.indexOf("SURNAME") == -1)
            return false;
        return true;
    }

    public TrustDomainEntity getTrustDomain()
            throws TrustDomainNotFoundException {

        return trustDomainDAO.getTrustDomain(TRUST_DOMAIN_NAME);
    }

    public PkiProvider getReference() {

        LOG.debug("get reference");
        return context.getBusinessObject(PkiProvider.class);
    }

    public String getIdentifierDomainName() {

        return IDENTIFIER_DOMAIN_NAME;
    }

    public String parseIdentifierFromCert(X509Certificate certificate) {

        try {
            byte[] data = certificate.getEncoded();
            return DigestUtils.shaHex(data);
        }

        catch (CertificateEncodingException e) {
            throw new IllegalArgumentException("cert can't be encoded: " + e.getMessage());
        }
    }

    /**
     * Parse the given attribute out of the given DN.
     */
    private String parseAttributeFromCert(X509Certificate certificate, String attributeName) {

        String subjectName = certificate.getSubjectX500Principal().toString();
        int attributeBegin = subjectName.indexOf(attributeName + '=');
        if (attributeBegin < 0)
            throw new IllegalArgumentException("attribute name does not occur in subject: " + attributeName);

        attributeBegin += attributeName.length() + 1; // "attributeName="
        int attributeEnd = subjectName.indexOf(',', attributeBegin);
        if (attributeEnd < 0) {
            // last field has no trailing ","
            attributeEnd = subjectName.length();
        }

        return subjectName.substring(attributeBegin, attributeEnd);
    }

    public void storeDeviceAttributes(SubjectEntity subject, String surname, String givenName, X509Certificate certificate) {

        try {
            // Some attribute values we'll need to assign.
            // Identifier attribute used for removal, no authentication is required in that step so no access to the eID certificate.
            String userAttributeValue = getUserAttributeValue(givenName, surname);
            String nrn = parseAttributeFromCert(certificate, "SERIALNUMBER");
            String identifier = parseIdentifierFromCert(certificate);

            // Create the compound attribute for the BeID device.
            CompoundAttributeDO deviceAttribute = attributeManager.newCompound(BeIdConstants.BEID_DEVICE_ATTRIBUTE, subject);
            deviceAttribute.addAttribute(BeIdConstants.BEID_SURNAME_ATTRIBUTE, surname);
            deviceAttribute.addAttribute(BeIdConstants.BEID_GIVENNAME_ATTRIBUTE, givenName);
            deviceAttribute.addAttribute(BeIdConstants.BEID_NRN_ATTRIBUTE, nrn);
            deviceAttribute.addAttribute(BeIdConstants.BEID_IDENTIFIER_ATTRIBUTE, identifier);
            deviceAttribute.addAttribute(BeIdConstants.BEID_DEVICE_USER_ATTRIBUTE, userAttributeValue);
            deviceAttribute.addAttribute(BeIdConstants.BEID_DEVICE_DISABLE_ATTRIBUTE, false);
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Missing attribute type for BeID device", e);
        }
    }

    public boolean isDisabled(SubjectEntity subject, X509Certificate certificate)
            throws DeviceRegistrationNotFoundException {

        String nrn = parseAttributeFromCert(certificate, "SERIALNUMBER");

        // Find the device to enable based on the certificate's NRN.
        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, BeIdConstants.BEID_DEVICE_ATTRIBUTE,
                    BeIdConstants.BEID_NRN_ATTRIBUTE, nrn);
            AttributeEntity disableAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    BeIdConstants.BEID_DEVICE_DISABLE_ATTRIBUTE);

            return disableAttribute.getBooleanValue();
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Missing attribute type for BeID device", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String attributeId)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = subjectService.getSubject(userId);

        // Find the device to enable based on the user attribute (surname, givenName).
        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, BeIdConstants.BEID_DEVICE_ATTRIBUTE, attributeId);
            AttributeEntity disableAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    BeIdConstants.BEID_DEVICE_DISABLE_ATTRIBUTE);

            disableAttribute.setValue(true);
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Missing attribute type for BeID device", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void enable(SubjectEntity subject, X509Certificate certificate)
            throws PermissionDeniedException, DeviceRegistrationNotFoundException {

        String nrn = parseAttributeFromCert(certificate, "SERIALNUMBER");

        // Find the device to enable based on the certificate's NRN.
        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, BeIdConstants.BEID_DEVICE_ATTRIBUTE,
                    BeIdConstants.BEID_NRN_ATTRIBUTE, nrn);
            AttributeEntity disableAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    BeIdConstants.BEID_DEVICE_DISABLE_ATTRIBUTE);

            disableAttribute.setValue(false);
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Missing attribute type for BeID device", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String userId, String attributeId)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = subjectService.getSubject(userId);

        // Find the device to remove.
        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, BeIdConstants.BEID_DEVICE_ATTRIBUTE, attributeId);

            // Find the device's certificate identifier which we need to remove the subject.
            AttributeEntity identifierAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    BeIdConstants.BEID_IDENTIFIER_ATTRIBUTE);
            String identifier = identifierAttribute.getStringValue();

            // Remove the subject & the device compound attribute.
            subjectIdentifierDAO.removeSubjectIdentifier(subject, getIdentifierDomainName(), identifier);
            attributeManager.removeCompoundWhere(subject, BeIdConstants.BEID_DEVICE_ATTRIBUTE, BeIdConstants.BEID_IDENTIFIER_ATTRIBUTE,
                    identifier);
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Missing attribute type for BeID device", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    private String getUserAttributeValue(String givenName, String surname) {

        return String.format("%s, %s", surname, givenName);
    }
}
