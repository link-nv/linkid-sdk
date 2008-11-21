/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.otpoversms.bean;

import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;
import net.link.safeonline.otpoversms.keystore.OtpOverSmsKeyStoreUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = OtpOverSmsStartableBean.JNDI_BINDING)
public class OtpOverSmsStartableBean extends AbstractInitBean {

    public static final String JNDI_BINDING = OtpOverSmsConstants.OTPOVERSMS_STARTABLE_JNDI_PREFIX + "OtpOverSmsStartableBean";


    public OtpOverSmsStartableBean() {

        configureNode();

        configureDevice();
    }

    private void configureNode() {

        ResourceBundle properties = ResourceBundle.getBundle("otpoversms_config");
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

    private void configureDevice() {

        X509Certificate certificate = (X509Certificate) OtpOverSmsKeyStoreUtils.getPrivateKeyEntry().getCertificate();

        ResourceBundle properties = ResourceBundle.getBundle("otpoversms_config");
        String nodeName = properties.getString("olas.node.name");
        String otpOverSmsWebappName = properties.getString("otpoversms.webapp.name");

        AttributeTypeEntity otpOverSmsMobileAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE,
                DatatypeType.STRING, true, false);
        otpOverSmsMobileAttributeType.setMultivalued(true);
        this.attributeTypes.add(otpOverSmsMobileAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsMobileAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsMobileAttributeType, "nl", "Gsm nummer", null));

        AttributeTypeEntity pinHashAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_PIN_HASH_ATTRIBUTE,
                DatatypeType.STRING, false, false);
        pinHashAttributeType.setMultivalued(true);
        AttributeTypeEntity pinSeedAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_PIN_SEED_ATTRIBUTE,
                DatatypeType.STRING, false, false);
        pinSeedAttributeType.setMultivalued(true);
        AttributeTypeEntity pinAlgorithmAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_PIN_ALGORITHM_ATTRIBUTE,
                DatatypeType.STRING, false, false);
        pinAlgorithmAttributeType.setMultivalued(true);
        this.attributeTypes.add(pinHashAttributeType);
        this.attributeTypes.add(pinSeedAttributeType);
        this.attributeTypes.add(pinAlgorithmAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinHashAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile Lite Pin hash", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinSeedAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile Lite Pin hash seed", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinAlgorithmAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile Lite Pin hash algorithm", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinHashAttributeType, "nl", "GSM lite pin hash", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinSeedAttributeType, "nl", "GSM lite hash seed", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinAlgorithmAttributeType, "nl",
                "GSM lite pin hash algoritme", null));

        AttributeTypeEntity otpOverSmsDeviceDisableAttributeType = new AttributeTypeEntity(
                OtpOverSmsConstants.OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE, DatatypeType.BOOLEAN, false, false);
        otpOverSmsDeviceDisableAttributeType.setMultivalued(true);
        this.attributeTypes.add(otpOverSmsDeviceDisableAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsDeviceDisableAttributeType,
                Locale.ENGLISH.getLanguage(), "Mobile Lite Disable Attribute", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsDeviceDisableAttributeType, "nl",
                "Mobile Lite Disable Attribuut", null));

        AttributeTypeEntity otpOverSmsDeviceAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE,
                DatatypeType.COMPOUNDED, false, false);
        otpOverSmsDeviceAttributeType.setMultivalued(true);
        otpOverSmsDeviceAttributeType.addMember(otpOverSmsMobileAttributeType, 0, true);
        otpOverSmsDeviceAttributeType.addMember(pinHashAttributeType, 1, true);
        otpOverSmsDeviceAttributeType.addMember(pinSeedAttributeType, 2, true);
        otpOverSmsDeviceAttributeType.addMember(pinAlgorithmAttributeType, 3, true);
        otpOverSmsDeviceAttributeType.addMember(otpOverSmsDeviceDisableAttributeType, 4, true);
        this.attributeTypes.add(otpOverSmsDeviceAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsDeviceAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile Lite", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsDeviceAttributeType, "nl", "GSM Lite", null));

        this.devices.add(new Device(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID, SafeOnlineConstants.MOBILE_DEVICE_CLASS, nodeName, "/"
                + otpOverSmsWebappName + "/auth", "/" + otpOverSmsWebappName + "/device", "/" + otpOverSmsWebappName + "/device", "/"
                + otpOverSmsWebappName + "/device", "/" + otpOverSmsWebappName + "/device", certificate, otpOverSmsDeviceAttributeType,
                otpOverSmsMobileAttributeType, otpOverSmsDeviceDisableAttributeType));
        this.deviceDescriptions.add(new DeviceDescription(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID, "nl", "GSM Lite"));
        this.deviceDescriptions.add(new DeviceDescription(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID, Locale.ENGLISH.getLanguage(),
                "Mobile Lite"));
        this.trustedCertificates.put(certificate, SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN);

    }

    @Override
    public void preStop() {

        this.LOG.debug("pre stop");
    }

    @Override
    public int getPriority() {

        return OtpOverSmsConstants.OTPOVERSMS_BOOT_PRIORITY;
    }
}