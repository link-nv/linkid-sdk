/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.encap.bean;

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
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.util.servlet.SafeOnlineConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = EncapStartableBean.JNDI_BINDING)
public class EncapStartableBean extends AbstractInitBean {

    public static final String JNDI_BINDING = EncapConstants.ENCAP_STARTABLE_JNDI_PREFIX + "EncapStartableBean";

    private static final Log   LOG          = LogFactory.getLog(EncapStartableBean.class);


    /**
     * {@inheritDoc}
     */
    @Override
    public void postStart() {

        configureNode();

        AttributeTypeEntity encapMobileAttributeType = new AttributeTypeEntity(EncapConstants.ENCAP_MOBILE_ATTRIBUTE, DatatypeType.STRING,
                true, false);
        encapMobileAttributeType.setMultivalued(true);
        attributeTypes.add(encapMobileAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapMobileAttributeType, Locale.ENGLISH.getLanguage(), "Mobile",
                null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapMobileAttributeType, "nl", "Gsm nummer", null));

        AttributeTypeEntity encapDeviceDisableAttributeType = new AttributeTypeEntity(EncapConstants.ENCAP_DEVICE_DISABLE_ATTRIBUTE,
                DatatypeType.BOOLEAN, false, false);
        encapDeviceDisableAttributeType.setMultivalued(true);
        attributeTypes.add(encapDeviceDisableAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapDeviceDisableAttributeType, Locale.ENGLISH.getLanguage(),
                "Encap Disable Attribute", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapDeviceDisableAttributeType, "nl", "Encap Disable Attribuut",
                null));

        AttributeTypeEntity encapDeviceAttributeType = new AttributeTypeEntity(EncapConstants.ENCAP_DEVICE_ATTRIBUTE,
                DatatypeType.COMPOUNDED, false, false);
        encapDeviceAttributeType.setMultivalued(true);
        encapDeviceAttributeType.addMember(encapMobileAttributeType, 0, true);
        encapDeviceAttributeType.addMember(encapDeviceDisableAttributeType, 1, true);
        attributeTypes.add(encapDeviceAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapDeviceAttributeType, Locale.ENGLISH.getLanguage(), "Encap",
                null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapDeviceAttributeType, "nl", "Encap", null));

        ResourceBundle properties = ResourceBundle.getBundle("encap_config");
        String encapWebappName = properties.getString("encap.webapp.name");
        String encapAuthWSPath = properties.getString("encap.auth.ws.webapp.name");

        devices.add(new Device(EncapConstants.ENCAP_DEVICE_ID, SafeOnlineConstants.MOBILE_DEVICE_CLASS, SafeOnlineConfig.nodeName(),
                "/olas-encap/auth", "/" + encapAuthWSPath, "/" + encapWebappName + "/device", "/" + encapWebappName + "/device", null, "/"
                        + encapWebappName + "/device", "/" + encapWebappName + "/device", encapDeviceAttributeType,
                encapMobileAttributeType, encapDeviceDisableAttributeType));
        deviceDescriptions.add(new DeviceDescription(EncapConstants.ENCAP_DEVICE_ID, "nl", "GSM"));
        deviceDescriptions.add(new DeviceDescription(EncapConstants.ENCAP_DEVICE_ID, Locale.ENGLISH.getLanguage(), "Mobile"));

        super.postStart();
    }

    private void configureNode() {

        SafeOnlineNodeKeyStore nodeKeyStore = new SafeOnlineNodeKeyStore();

        node = new Node(SafeOnlineConfig.nodeName(), SafeOnlineConfig.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
                SafeOnlineConfig.nodePort(), SafeOnlineConfig.nodePortSecure(), nodeKeyStore.getCertificate());
        trustedCertificates.put(nodeKeyStore.getCertificate(), SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
    }

    @Override
    public void preStop() {

        LOG.debug("pre stop");
    }

    @Override
    public int getPriority() {

        return EncapConstants.ENCAP_BOOT_PRIORITY;
    }
}
