/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.beid.bean;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.beid.keystore.BeidKeyStoreUtils;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.beid.BeIdPkiProvider;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.TrustPointDAO;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = BeIdConstants.BEID_STARTABLE_JNDI_PREFIX + "BeIdStartableBean")
public class BeIdStartableBean extends AbstractInitBean {

    private static final Log   LOG            = LogFactory.getLog(BeIdStartableBean.class);

    public static final String INDEX_RESOURCE = "certs/beid/index.txt";

    @EJB
    private TrustDomainDAO     trustDomainDAO;

    @EJB
    private TrustPointDAO      trustPointDAO;


    public BeIdStartableBean() {

        configureNode();

        AttributeTypeEntity givenNameAttributeType = new AttributeTypeEntity(BeIdConstants.GIVENNAME_ATTRIBUTE, DatatypeType.STRING, true,
                false);
        givenNameAttributeType.setMultivalued(true);
        this.attributeTypes.add(givenNameAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(givenNameAttributeType, Locale.ENGLISH.getLanguage(),
                "Given name (BeID)", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(givenNameAttributeType, "nl", "Naam (BeID)", null));

        AttributeTypeEntity surnameAttributeType = new AttributeTypeEntity(BeIdConstants.SURNAME_ATTRIBUTE, DatatypeType.STRING, true,
                false);
        surnameAttributeType.setMultivalued(true);
        this.attributeTypes.add(surnameAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(surnameAttributeType, Locale.ENGLISH.getLanguage(),
                "Surname (BeID)", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(surnameAttributeType, "nl", "Achternaam (BeID)", null));

        AttributeTypeEntity nrnAttributeType = new AttributeTypeEntity(BeIdConstants.NRN_ATTRIBUTE, DatatypeType.STRING, true, false);
        nrnAttributeType.setMultivalued(true);
        this.attributeTypes.add(nrnAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(nrnAttributeType, Locale.ENGLISH.getLanguage(),
                "Identification number of the National Register", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(nrnAttributeType, "nl",
                "Identificatienummer van het Rijksregister", null));

        AttributeTypeEntity beidDeviceUserAttributeType = new AttributeTypeEntity(BeIdConstants.BEID_DEVICE_USER_ATTRIBUTE,
                DatatypeType.STRING, true, false);
        beidDeviceUserAttributeType.setMultivalued(true);
        this.attributeTypes.add(beidDeviceUserAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceUserAttributeType, Locale.ENGLISH.getLanguage(),
                "BeID Name", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceUserAttributeType, "nl", "BeID Naam", null));

        AttributeTypeEntity beidDeviceDisableAttributeType = new AttributeTypeEntity(BeIdConstants.BEID_DEVICE_DISABLE_ATTRIBUTE,
                DatatypeType.BOOLEAN, false, false);
        beidDeviceDisableAttributeType.setMultivalued(true);
        this.attributeTypes.add(beidDeviceDisableAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceDisableAttributeType, Locale.ENGLISH.getLanguage(),
                "BeID Disable Attribute", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceDisableAttributeType, "nl",
                "BeID Disable Attribuut", null));

        AttributeTypeEntity beidDeviceAttributeType = new AttributeTypeEntity(BeIdConstants.BEID_DEVICE_ATTRIBUTE, DatatypeType.COMPOUNDED,
                true, false);
        beidDeviceAttributeType.setMultivalued(true);
        beidDeviceAttributeType.addMember(givenNameAttributeType, 0, true);
        beidDeviceAttributeType.addMember(surnameAttributeType, 1, true);
        beidDeviceAttributeType.addMember(nrnAttributeType, 2, true);
        beidDeviceAttributeType.addMember(beidDeviceUserAttributeType, 3, true);
        beidDeviceAttributeType.addMember(beidDeviceDisableAttributeType, 4, true);
        this.attributeTypes.add(beidDeviceAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceAttributeType, Locale.ENGLISH.getLanguage(),
                "BeID", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceAttributeType, "nl", "BeID", null));

        X509Certificate certificate = (X509Certificate) BeidKeyStoreUtils.getPrivateKeyEntry().getCertificate();
        this.trustedCertificates.put(certificate, SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN);

        ResourceBundle properties = ResourceBundle.getBundle("config");
        String nodeName = properties.getString("olas.node.name");

        this.devices.add(new Device(BeIdConstants.BEID_DEVICE_ID, SafeOnlineConstants.PKI_DEVICE_CLASS, nodeName, "/olas-beid/auth",
                "/olas-beid/device", "/olas-beid/device", null, "/olas-beid/device", certificate, beidDeviceAttributeType,
                beidDeviceUserAttributeType, beidDeviceDisableAttributeType));
        this.deviceDescriptions.add(new DeviceDescription(BeIdConstants.BEID_DEVICE_ID, "nl", "Belgische eID"));
        this.deviceDescriptions.add(new DeviceDescription(BeIdConstants.BEID_DEVICE_ID, Locale.ENGLISH.getLanguage(), "Belgian eID"));
    }

    @Override
    public void postStart() {

        LOG.debug("post start");
        super.postStart();
        initTrustDomain();
    }

    @SuppressWarnings("unchecked")
    public void initTrustDomain() {

        TrustDomainEntity beidTrustDomain = this.trustDomainDAO.findTrustDomain(BeIdPkiProvider.TRUST_DOMAIN_NAME);
        if (null != beidTrustDomain)
            return;

        beidTrustDomain = this.trustDomainDAO.addTrustDomain(BeIdPkiProvider.TRUST_DOMAIN_NAME, true);

        CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            LOG.error("could not get cert factory: " + e.getMessage(), e);
            return;
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        InputStream indexInputStream = classLoader.getResourceAsStream(INDEX_RESOURCE);
        List<String> lines;
        try {
            lines = IOUtils.readLines(indexInputStream);
        } catch (IOException e) {
            LOG.error("could not read the BeID certificate index file");
            return;
        }
        IOUtils.closeQuietly(indexInputStream);

        for (String certFilename : lines) {
            LOG.debug("loading " + certFilename);
            InputStream certInputStream = classLoader.getResourceAsStream("certs/beid/" + certFilename);
            X509Certificate certificate;
            try {
                certificate = (X509Certificate) certificateFactory.generateCertificate(certInputStream);
                this.trustPointDAO.addTrustPoint(beidTrustDomain, certificate);
            } catch (CertificateException e) {
                LOG.error("certificate error: " + e.getMessage(), e);
            }
        }
    }

    private void configureNode() {

        ResourceBundle properties = ResourceBundle.getBundle("config");
        String nodeName = properties.getString("olas.node.name");
        String protocol = properties.getString("olas.host.protocol");
        String hostname = properties.getString("olas.host.name");
        int hostport = Integer.parseInt(properties.getString("olas.host.port"));
        int hostportssl = Integer.parseInt(properties.getString("olas.host.port.ssl"));

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        IdentityServiceClient identityServiceClient = new IdentityServiceClient();

        this.node = new Node(nodeName, protocol, hostname, hostport, hostportssl, authIdentityServiceClient.getCertificate(),
                identityServiceClient.getCertificate());
        this.trustedCertificates.put(authIdentityServiceClient.getCertificate(), SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
    }

    @Override
    public void preStop() {

        LOG.debug("pre stop");
    }

    @Override
    public int getPriority() {

        return BeIdConstants.BEID_BOOT_PRIORITY;
    }
}
