/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.encap.bean;

import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.encap.keystore.EncapKeyStoreUtils;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = EncapStartableBean.JNDI_BINDING)
public class EncapStartableBean extends AbstractInitBean {

    public static final String JNDI_BINDING = EncapConstants.ENCAP_STARTABLE_JNDI_PREFIX + "EncapStartableBean";

    private static final Log   LOG          = LogFactory.getLog(EncapStartableBean.class);


    public EncapStartableBean() {

        configureNode();

        AttributeTypeEntity encapMobileAttributeType = new AttributeTypeEntity(EncapConstants.ENCAP_MOBILE_ATTRIBUTE, DatatypeType.STRING,
                true, false);
        encapMobileAttributeType.setMultivalued(true);
        this.attributeTypes.add(encapMobileAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapMobileAttributeType, Locale.ENGLISH.getLanguage(),
                "Mobile", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapMobileAttributeType, "nl", "Gsm nummer", null));

        AttributeTypeEntity encapDeviceDisableAttributeType = new AttributeTypeEntity(EncapConstants.ENCAP_DEVICE_DISABLE_ATTRIBUTE,
                DatatypeType.BOOLEAN, false, false);
        encapDeviceDisableAttributeType.setMultivalued(true);
        this.attributeTypes.add(encapDeviceDisableAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapDeviceDisableAttributeType,
                Locale.ENGLISH.getLanguage(), "Encap Disable Attribute", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapDeviceDisableAttributeType, "nl",
                "Encap Disable Attribuut", null));

        AttributeTypeEntity encapDeviceAttributeType = new AttributeTypeEntity(EncapConstants.ENCAP_DEVICE_ATTRIBUTE,
                DatatypeType.COMPOUNDED, true, false);
        encapDeviceAttributeType.setMultivalued(true);
        encapDeviceAttributeType.addMember(encapMobileAttributeType, 0, true);
        encapDeviceAttributeType.addMember(encapDeviceDisableAttributeType, 1, true);
        this.attributeTypes.add(encapDeviceAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapDeviceAttributeType, Locale.ENGLISH.getLanguage(),
                "Encap", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(encapDeviceAttributeType, "nl", "Encap", null));

        X509Certificate certificate = (X509Certificate) EncapKeyStoreUtils.getPrivateKeyEntry().getCertificate();

        ResourceBundle properties = ResourceBundle.getBundle("encap_config");
        String nodeName = properties.getString("olas.node.name");
        String encapWebappName = properties.getString("encap.webapp.name");
        String encapAuthWSPath = properties.getString("encap.auth.ws.webapp.name");

        this.devices.add(new Device(EncapConstants.ENCAP_DEVICE_ID, SafeOnlineConstants.MOBILE_DEVICE_CLASS, nodeName, "/olas-encap/auth",
                "/" + encapAuthWSPath, "/" + encapWebappName + "/device", "/" + encapWebappName + "/device", null, "/" + encapWebappName
                        + "/device", "/" + encapWebappName + "/device", certificate, encapDeviceAttributeType, encapMobileAttributeType,
                encapDeviceDisableAttributeType));
        this.deviceDescriptions.add(new DeviceDescription(EncapConstants.ENCAP_DEVICE_ID, "nl", "GSM"));
        this.deviceDescriptions.add(new DeviceDescription(EncapConstants.ENCAP_DEVICE_ID, Locale.ENGLISH.getLanguage(), "Mobile"));
        this.trustedCertificates.put(certificate, SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN);
    }

    private void configureNode() {

        ResourceBundle properties = ResourceBundle.getBundle("encap_config");
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

        return EncapConstants.ENCAP_BOOT_PRIORITY;
    }
}
