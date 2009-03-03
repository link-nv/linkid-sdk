/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.digipass.bean;

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
import net.link.safeonline.model.digipass.DigipassConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = DigipassStartableBean.JNDI_BINDING)
public class DigipassStartableBean extends AbstractInitBean {

    public static final String JNDI_BINDING = DigipassConstants.DIGIPASS_STARTABLE_JNDI_PREFIX + "DigipassStartableBean";

    private static final Log   LOG          = LogFactory.getLog(DigipassStartableBean.class);


    @Override
    public void postStart() {

        configureNode();

        AttributeTypeEntity digipassSNAttributeType = new AttributeTypeEntity(DigipassConstants.DIGIPASS_SN_ATTRIBUTE, DatatypeType.STRING,
                true, false);
        digipassSNAttributeType.setMultivalued(true);
        attributeTypes.add(digipassSNAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassSNAttributeType, Locale.ENGLISH.getLanguage(),
                "Digipass Serial number", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassSNAttributeType, "nl", "Digipass Serie nummer", null));

        AttributeTypeEntity digipassDeviceDisableAttributeType = new AttributeTypeEntity(
                DigipassConstants.DIGIPASS_DEVICE_DISABLE_ATTRIBUTE, DatatypeType.BOOLEAN, false, false);
        digipassDeviceDisableAttributeType.setMultivalued(true);
        attributeTypes.add(digipassDeviceDisableAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassDeviceDisableAttributeType, Locale.ENGLISH.getLanguage(),
                "Digipass Disable Attribute", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassDeviceDisableAttributeType, "nl",
                "Digipass Disable Attribuut", null));

        AttributeTypeEntity digipassDeviceAttributeType = new AttributeTypeEntity(DigipassConstants.DIGIPASS_DEVICE_ATTRIBUTE,
                DatatypeType.COMPOUNDED, true, false);
        digipassDeviceAttributeType.setMultivalued(true);
        digipassDeviceAttributeType.addMember(digipassSNAttributeType, 0, true);
        digipassDeviceAttributeType.addMember(digipassDeviceDisableAttributeType, 1, true);
        attributeTypes.add(digipassDeviceAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassDeviceAttributeType, Locale.ENGLISH.getLanguage(),
                "Digipass", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassDeviceAttributeType, "nl", "Digipass", null));

        ResourceBundle properties = ResourceBundle.getBundle("digipass_config");
        String nodeName = properties.getString("olas.node.name");
        String digipassWebappName = properties.getString("digipass.webapp.name");

        LOG.debug("adding digipass device");
        devices.add(new Device(DigipassConstants.DIGIPASS_DEVICE_ID, SafeOnlineConstants.DIGIPASS_DEVICE_CLASS, nodeName, "/"
                + digipassWebappName + "/auth", null, null, null, null, "/" + digipassWebappName + "/device", "/" + digipassWebappName
                + "/device", digipassDeviceAttributeType, digipassSNAttributeType, digipassDeviceDisableAttributeType));
        deviceDescriptions.add(new DeviceDescription(DigipassConstants.DIGIPASS_DEVICE_ID, "nl", "EBank Digipass"));
        deviceDescriptions.add(new DeviceDescription(DigipassConstants.DIGIPASS_DEVICE_ID, Locale.ENGLISH.getLanguage(), "EBank Digipass"));

        // now initialize
        super.postStart();
    }

    private void configureNode() {

        ResourceBundle properties = ResourceBundle.getBundle("digipass_config");
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

        return DigipassConstants.DIGIPASS_BOOT_PRIORITY;
    }
}
