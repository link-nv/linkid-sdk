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
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.TrustPointDAO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = BeIdStartableBean.JNDI_BINDING)
public class BeIdStartableBean extends AbstractInitBean {

    public static final String JNDI_BINDING   = BeIdConstants.BEID_STARTABLE_JNDI_PREFIX + "BeIdStartableBean";

    private static final Log   LOG            = LogFactory.getLog(BeIdStartableBean.class);

    public static final String INDEX_RESOURCE = "certs/beid/index";

    @EJB(mappedName = TrustDomainDAO.JNDI_BINDING)
    private TrustDomainDAO     trustDomainDAO;

    @EJB(mappedName = TrustPointDAO.JNDI_BINDING)
    private TrustPointDAO      trustPointDAO;


    @Override
    public void postStart() {

        LOG.debug("post start");

        configureNode();

        AttributeTypeEntity givenNameAttributeType = new AttributeTypeEntity(BeIdConstants.BEID_GIVENNAME_ATTRIBUTE, DatatypeType.STRING,
                true, false);
        givenNameAttributeType.setMultivalued(true);
        attributeTypes.add(givenNameAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(givenNameAttributeType, Locale.ENGLISH.getLanguage(),
                "Given name (BeID)", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(givenNameAttributeType, "nl", "Naam (BeID)", null));

        AttributeTypeEntity surnameAttributeType = new AttributeTypeEntity(BeIdConstants.BEID_SURNAME_ATTRIBUTE, DatatypeType.STRING, true,
                false);
        surnameAttributeType.setMultivalued(true);
        attributeTypes.add(surnameAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(surnameAttributeType, Locale.ENGLISH.getLanguage(),
                "Surname (BeID)", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(surnameAttributeType, "nl", "Achternaam (BeID)", null));

        AttributeTypeEntity nrnAttributeType = new AttributeTypeEntity(BeIdConstants.BEID_NRN_ATTRIBUTE, DatatypeType.STRING, true, false);
        nrnAttributeType.setMultivalued(true);
        attributeTypes.add(nrnAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(nrnAttributeType, Locale.ENGLISH.getLanguage(),
                "Identification number of the National Register", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(nrnAttributeType, "nl",
                "Identificatienummer van het Rijksregister", null));

        AttributeTypeEntity identifierAttributeType = new AttributeTypeEntity(BeIdConstants.BEID_IDENTIFIER_ATTRIBUTE, DatatypeType.STRING,
                false, false);
        identifierAttributeType.setMultivalued(true);
        attributeTypes.add(identifierAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(identifierAttributeType, Locale.ENGLISH.getLanguage(),
                "Identifier (BeID)", null));
        attributeTypeDescriptions
                                 .add(new AttributeTypeDescriptionEntity(identifierAttributeType, "nl", "Identificatienummer (BeID)", null));

        AttributeTypeEntity beidDeviceUserAttributeType = new AttributeTypeEntity(BeIdConstants.BEID_DEVICE_USER_ATTRIBUTE,
                DatatypeType.STRING, true, false);
        beidDeviceUserAttributeType.setMultivalued(true);
        attributeTypes.add(beidDeviceUserAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceUserAttributeType, Locale.ENGLISH.getLanguage(),
                "BeID Name", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceUserAttributeType, "nl", "BeID Naam", null));

        AttributeTypeEntity beidDeviceDisableAttributeType = new AttributeTypeEntity(BeIdConstants.BEID_DEVICE_DISABLE_ATTRIBUTE,
                DatatypeType.BOOLEAN, false, false);
        beidDeviceDisableAttributeType.setMultivalued(true);
        attributeTypes.add(beidDeviceDisableAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceDisableAttributeType, Locale.ENGLISH.getLanguage(),
                "BeID Disable Attribute", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceDisableAttributeType, "nl", "BeID Disable Attribuut",
                null));

        AttributeTypeEntity beidDeviceAttributeType = new AttributeTypeEntity(BeIdConstants.BEID_DEVICE_ATTRIBUTE, DatatypeType.COMPOUNDED,
                true, false);
        beidDeviceAttributeType.setMultivalued(true);
        beidDeviceAttributeType.addMember(givenNameAttributeType, 0, true);
        beidDeviceAttributeType.addMember(surnameAttributeType, 1, true);
        beidDeviceAttributeType.addMember(nrnAttributeType, 2, true);
        beidDeviceAttributeType.addMember(identifierAttributeType, 3, true);
        beidDeviceAttributeType.addMember(beidDeviceUserAttributeType, 4, true);
        beidDeviceAttributeType.addMember(beidDeviceDisableAttributeType, 5, true);
        attributeTypes.add(beidDeviceAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceAttributeType, Locale.ENGLISH.getLanguage(), "BeID",
                null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(beidDeviceAttributeType, "nl", "BeID", null));

        ResourceBundle properties = ResourceBundle.getBundle("beid_config");
        String nodeName = properties.getString("olas.node.name");
        String beidWebappName = properties.getString("beid.webapp.name");
        String beidAuthWSPath = properties.getString("beid.auth.ws.webapp.name");

        devices.add(new Device(BeIdConstants.BEID_DEVICE_ID, SafeOnlineConstants.PKI_DEVICE_CLASS, nodeName, "/" + beidWebappName
                + "/_auth", "/" + beidAuthWSPath, "/" + beidWebappName + "/_device", "/" + beidWebappName + "/_device", null, "/"
                + beidWebappName + "/_device", "/" + beidWebappName + "/_device", beidDeviceAttributeType, beidDeviceUserAttributeType,
                beidDeviceDisableAttributeType));
        deviceDescriptions.add(new DeviceDescription(BeIdConstants.BEID_DEVICE_ID, "nl", "Belgische eID"));
        deviceDescriptions.add(new DeviceDescription(BeIdConstants.BEID_DEVICE_ID, Locale.ENGLISH.getLanguage(), "Belgian eID"));

        // now initialize
        super.postStart();

        initTrustDomain();
    }

    @SuppressWarnings("unchecked")
    public void initTrustDomain() {

        TrustDomainEntity beidTrustDomain = trustDomainDAO.findTrustDomain(BeIdPkiProviderBean.TRUST_DOMAIN_NAME);
        if (null != beidTrustDomain)
            return;

        beidTrustDomain = trustDomainDAO.addTrustDomain(BeIdPkiProviderBean.TRUST_DOMAIN_NAME, true);

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
                trustPointDAO.addTrustPoint(beidTrustDomain, certificate);
            } catch (CertificateException e) {
                LOG.error("certificate error: " + e.getMessage(), e);
            }
        }
    }

    private void configureNode() {

        ResourceBundle properties = ResourceBundle.getBundle("beid_config");
        String nodeName = properties.getString("olas.node.name");
        String protocol = properties.getString("olas.host.protocol");
        String hostname = properties.getString("olas.host.name");
        int hostport = Integer.parseInt(properties.getString("olas.host.port"));
        int hostportssl = Integer.parseInt(properties.getString("olas.host.port.ssl"));

        SafeOnlineNodeKeyStore nodeKeyStore = new SafeOnlineNodeKeyStore();

        node = new Node(nodeName, protocol, hostname, hostport, hostportssl, nodeKeyStore.getCertificate());
        trustedCertificates.put(nodeKeyStore.getCertificate(), SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
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
