/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.option.bean;

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
import net.link.safeonline.model.option.OptionConstants;
import net.link.safeonline.option.keystore.OptionKeyStoreUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = OptionStartableBean.JNDI_BINDING)
public class OptionStartableBean extends AbstractInitBean {

    public static final String JNDI_BINDING = OptionConstants.OPTION_STARTABLE_JNDI_PREFIX + "OptionStartableBean";

    private static final Log   LOG          = LogFactory.getLog(OptionStartableBean.class);


    public OptionStartableBean() {

        configureNode();

        AttributeTypeEntity optionImeiAttributeType = new AttributeTypeEntity(OptionConstants.OPTION_IMEI_ATTRIBUTE, DatatypeType.STRING,
                true, false);
        optionImeiAttributeType.setMultivalued(true);
        attributeTypes.add(optionImeiAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(optionImeiAttributeType, Locale.ENGLISH.getLanguage(), "IMEI",
                null));

        AttributeTypeEntity optionDeviceDisableAttributeType = new AttributeTypeEntity(OptionConstants.OPTION_DEVICE_DISABLE_ATTRIBUTE,
                DatatypeType.BOOLEAN, false, false);
        optionDeviceDisableAttributeType.setMultivalued(true);
        attributeTypes.add(optionDeviceDisableAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(optionDeviceDisableAttributeType, Locale.ENGLISH.getLanguage(),
                "Option Disable Attribute", null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(optionDeviceDisableAttributeType, "nl",
                "Option Disable Attribuut", null));

        AttributeTypeEntity optionDeviceAttributeType = new AttributeTypeEntity(OptionConstants.OPTION_DEVICE_ATTRIBUTE,
                DatatypeType.COMPOUNDED, true, false);
        optionDeviceAttributeType.setMultivalued(true);
        optionDeviceAttributeType.addMember(optionImeiAttributeType, 0, true);
        optionDeviceAttributeType.addMember(optionDeviceDisableAttributeType, 1, true);
        attributeTypes.add(optionDeviceAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(optionDeviceAttributeType, Locale.ENGLISH.getLanguage(), "Option",
                null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(optionDeviceAttributeType, "nl", "Option", null));

        X509Certificate certificate = (X509Certificate) OptionKeyStoreUtils.getPrivateKeyEntry().getCertificate();

        ResourceBundle properties = ResourceBundle.getBundle("option_config");
        String nodeName = properties.getString("olas.node.name");
        String optionWebappName = properties.getString("option.webapp.name");
        String optionAuthWSPath = properties.getString("option.auth.ws.webapp.name");

        devices.add(new Device(OptionConstants.OPTION_DEVICE_ID, SafeOnlineConstants.MOBILE_DEVICE_CLASS, nodeName, "/olas-option/auth",
                "/" + optionAuthWSPath, "/" + optionWebappName + "/device", "/" + optionWebappName + "/device", null, "/"
                        + optionWebappName + "/device", "/" + optionWebappName + "/device", certificate, optionDeviceAttributeType,
                optionImeiAttributeType, optionDeviceDisableAttributeType));
        deviceDescriptions.add(new DeviceDescription(OptionConstants.OPTION_DEVICE_ID, "nl", "Option Datakaart"));
        deviceDescriptions.add(new DeviceDescription(OptionConstants.OPTION_DEVICE_ID, Locale.ENGLISH.getLanguage(), "Option Datacard"));
        trustedCertificates.put(certificate, SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN);
    }

    private void configureNode() {

        ResourceBundle properties = ResourceBundle.getBundle("option_config");
        String nodeName = properties.getString("olas.node.name");
        String protocol = properties.getString("olas.host.protocol");
        String hostname = properties.getString("olas.host.name");
        int hostport = Integer.parseInt(properties.getString("olas.host.port"));
        int hostportssl = Integer.parseInt(properties.getString("olas.host.port.ssl"));

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        IdentityServiceClient identityServiceClient = new IdentityServiceClient();

        node = new Node(nodeName, protocol, hostname, hostport, hostportssl, authIdentityServiceClient.getCertificate(),
                identityServiceClient.getCertificate());
        trustedCertificates.put(authIdentityServiceClient.getCertificate(), SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
    }

    @Override
    public void preStop() {

        LOG.debug("pre stop");
    }

    @Override
    public int getPriority() {

        return OptionConstants.OPTION_BOOT_PRIORITY;
    }
}
