/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.otpoversms.bean;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;
import net.link.safeonline.util.servlet.SafeOnlineConfig;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = OtpOverSmsStartableBean.JNDI_BINDING)
public class OtpOverSmsStartableBean extends AbstractInitBean {

    public static final String JNDI_BINDING = OtpOverSmsConstants.OTPOVERSMS_STARTABLE_JNDI_PREFIX + "OtpOverSmsStartableBean";


    /**
     * {@inheritDoc}
     */
    @Override
    public void postStart() {

        configureNode();

        configureDevice();

        super.postStart();
    }

    private void configureNode() {

        SafeOnlineNodeKeyStore nodeKeyStore = new SafeOnlineNodeKeyStore();

        node = new Node(SafeOnlineConfig.nodeName(), SafeOnlineConfig.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
                SafeOnlineConfig.nodePort(), SafeOnlineConfig.nodePortSecure(), nodeKeyStore.getCertificate());
        trustedCertificates.put(nodeKeyStore.getCertificate(), SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
    }

    private void configureDevice() {

        ResourceBundle properties = ResourceBundle.getBundle("otpoversms_config");
        String otpOverSmsWebappName = properties.getString("otpoversms.webapp.name");
        String otpOverSmsAuthWSPath = properties.getString("otpoversms.auth.ws.webapp.name");

        AttributeTypeEntity otpOverSmsMobileAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE,
                DatatypeType.STRING, true, false);
        otpOverSmsMobileAttributeType.setMultivalued(true);
        attributeTypes.add(otpOverSmsMobileAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsMobileAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsMobileAttributeType, "nl", "Gsm nummer", null));

        AttributeTypeEntity pinHashAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_PIN_HASH_ATTRIBUTE,
                DatatypeType.STRING, false, false);
        pinHashAttributeType.setMultivalued(true);
        AttributeTypeEntity pinSeedAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_PIN_SEED_ATTRIBUTE,
                DatatypeType.STRING, false, false);
        pinSeedAttributeType.setMultivalued(true);
        AttributeTypeEntity pinAlgorithmAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_PIN_ALGORITHM_ATTRIBUTE,
                DatatypeType.STRING, false, false);
        AttributeTypeEntity pinNewAlgorithmAttributeType = new AttributeTypeEntity(
                OtpOverSmsConstants.OTPOVERSMS_PIN_NEW_ALGORITHM_ATTRIBUTE, DatatypeType.STRING, false, false);
        pinAlgorithmAttributeType.setMultivalued(true);
        pinNewAlgorithmAttributeType.setMultivalued(true);
        AttributeTypeEntity pinAttemptsAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_PIN_ATTEMPTS_ATTRIBUTE,
                DatatypeType.INTEGER, false, false);
        pinAttemptsAttributeType.setMultivalued(true);
        attributeTypes.add(pinHashAttributeType);
        attributeTypes.add(pinSeedAttributeType);
        attributeTypes.add(pinAlgorithmAttributeType);
        attributeTypes.add(pinNewAlgorithmAttributeType);
        attributeTypes.add(pinAttemptsAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinHashAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile Lite Pin hash", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinSeedAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile Lite Pin hash seed", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinAlgorithmAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile Lite Pin hash algorithm", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinNewAlgorithmAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile Lite Pin new hash algorithm", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinAttemptsAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile Lite Pin Attempts", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinHashAttributeType, "nl", "GSM lite pin hash", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinSeedAttributeType, "nl", "GSM lite hash seed", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinAlgorithmAttributeType, "nl", "GSM lite pin hash algoritme",
                null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinNewAlgorithmAttributeType, "nl",
                "GSM lite pin nieuw hash algoritme", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinAttemptsAttributeType, "nl", "GSM lite pin pogingen", null));

        AttributeTypeEntity otpOverSmsDeviceDisableAttributeType = new AttributeTypeEntity(
                OtpOverSmsConstants.OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE, DatatypeType.BOOLEAN, false, false);
        otpOverSmsDeviceDisableAttributeType.setMultivalued(true);
        attributeTypes.add(otpOverSmsDeviceDisableAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsDeviceDisableAttributeType,
                Locale.ENGLISH.getLanguage(), "Mobile Lite Disable Attribute", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsDeviceDisableAttributeType, "nl",
                "Mobile Lite Disable Attribuut", null));

        AttributeTypeEntity otpOverSmsDeviceAttributeType = new AttributeTypeEntity(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE,
                DatatypeType.COMPOUNDED, false, false);
        otpOverSmsDeviceAttributeType.setMultivalued(true);
        otpOverSmsDeviceAttributeType.addMember(otpOverSmsMobileAttributeType, 0, true);
        otpOverSmsDeviceAttributeType.addMember(pinHashAttributeType, 1, true);
        otpOverSmsDeviceAttributeType.addMember(pinSeedAttributeType, 2, true);
        otpOverSmsDeviceAttributeType.addMember(pinAlgorithmAttributeType, 3, true);
        otpOverSmsDeviceAttributeType.addMember(pinNewAlgorithmAttributeType, 4, true);
        otpOverSmsDeviceAttributeType.addMember(pinAttemptsAttributeType, 5, true);
        otpOverSmsDeviceAttributeType.addMember(otpOverSmsDeviceDisableAttributeType, 6, true);
        attributeTypes.add(otpOverSmsDeviceAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsDeviceAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile Lite", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(otpOverSmsDeviceAttributeType, "nl", "GSM Lite", null));

        devices.add(new Device(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID, SafeOnlineConstants.MOBILE_DEVICE_CLASS,
                SafeOnlineConfig.nodeName(), "/" + otpOverSmsWebappName + "/auth", "/" + otpOverSmsAuthWSPath, "/" + otpOverSmsWebappName
                        + "/device", "/" + otpOverSmsWebappName + "/device", "/" + otpOverSmsWebappName + "/device", "/"
                        + otpOverSmsWebappName + "/device", "/" + otpOverSmsWebappName + "/device", otpOverSmsDeviceAttributeType,
                otpOverSmsMobileAttributeType, otpOverSmsDeviceDisableAttributeType));
        deviceDescriptions.add(new DeviceDescription(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID, "nl", "GSM Lite"));
        deviceDescriptions
                          .add(new DeviceDescription(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID, Locale.ENGLISH.getLanguage(), "Mobile Lite"));

    }

    @Override
    public void preStop() {

        LOG.debug("pre stop");
    }

    @Override
    public int getPriority() {

        return OtpOverSmsConstants.OTPOVERSMS_BOOT_PRIORITY;
    }
}
