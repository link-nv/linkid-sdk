/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.digipass.bean;

import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.digipass.keystore.DigipassKeyStoreUtils;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.digipass.DigipassConstants;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = DigipassStartableBean.JNDI_BINDING)
public class DigipassStartableBean extends AbstractInitBean {

    public static final String JNDI_BINDING = DigipassConstants.DIGIPASS_STARTABLE_JNDI_PREFIX + "DigipassStartableBean";

    private static final Log   LOG          = LogFactory.getLog(DigipassStartableBean.class);


    public DigipassStartableBean() {

        configureNode();

        AttributeTypeEntity digipassSNAttributeType = new AttributeTypeEntity(DigipassConstants.DIGIPASS_SN_ATTRIBUTE, DatatypeType.STRING,
                true, false);
        digipassSNAttributeType.setMultivalued(true);
        this.attributeTypes.add(digipassSNAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassSNAttributeType, Locale.ENGLISH.getLanguage(),
                "Digipass Serial number", null));
        this.attributeTypeDescriptions
                                      .add(new AttributeTypeDescriptionEntity(digipassSNAttributeType, "nl", "Digipass Serie nummer", null));

        AttributeTypeEntity digipassDeviceDisableAttributeType = new AttributeTypeEntity(
                DigipassConstants.DIGIPASS_DEVICE_DISABLE_ATTRIBUTE, DatatypeType.BOOLEAN, false, false);
        digipassDeviceDisableAttributeType.setMultivalued(true);
        this.attributeTypes.add(digipassDeviceDisableAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassDeviceDisableAttributeType,
                Locale.ENGLISH.getLanguage(), "Digipass Disable Attribute", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassDeviceDisableAttributeType, "nl",
                "Digipass Disable Attribuut", null));

        AttributeTypeEntity digipassDeviceAttributeType = new AttributeTypeEntity(DigipassConstants.DIGIPASS_DEVICE_ATTRIBUTE,
                DatatypeType.COMPOUNDED, true, false);
        digipassDeviceAttributeType.setMultivalued(true);
        digipassDeviceAttributeType.addMember(digipassSNAttributeType, 0, true);
        digipassDeviceAttributeType.addMember(digipassDeviceDisableAttributeType, 1, true);
        this.attributeTypes.add(digipassDeviceAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassDeviceAttributeType, Locale.ENGLISH.getLanguage(),
                "Digipass", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(digipassDeviceAttributeType, "nl", "Digipass", null));

        X509Certificate certificate = (X509Certificate) DigipassKeyStoreUtils.getPrivateKeyEntry().getCertificate();

        ResourceBundle properties = ResourceBundle.getBundle("digipass_config");
        String nodeName = properties.getString("olas.node.name");
        String digipassWebappName = properties.getString("digipass.webapp.name");

        this.devices.add(new Device(DigipassConstants.DIGIPASS_DEVICE_ID, SafeOnlineConstants.DIGIPASS_DEVICE_CLASS, nodeName, "/"
                + digipassWebappName + "/auth", null, null, null, "/" + digipassWebappName + "/device", "/" + digipassWebappName
                + "/device", certificate, digipassDeviceAttributeType, digipassSNAttributeType, digipassDeviceDisableAttributeType));
        this.deviceDescriptions.add(new DeviceDescription(DigipassConstants.DIGIPASS_DEVICE_ID, "nl", "EBank Digipass"));
        this.deviceDescriptions.add(new DeviceDescription(DigipassConstants.DIGIPASS_DEVICE_ID, Locale.ENGLISH.getLanguage(),
                "EBank Digipass"));
        this.trustedCertificates.put(certificate, SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN);
    }

    private void configureNode() {

        ResourceBundle properties = ResourceBundle.getBundle("digipass_config");
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

        return DigipassConstants.DIGIPASS_BOOT_PRIORITY;
    }
}
