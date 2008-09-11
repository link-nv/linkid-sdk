/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
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

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link OptionStartableBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 8, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = OptionConstants.OPTION_STARTABLE_JNDI_PREFIX + "OptionStartableBean")
public class OptionStartableBean extends AbstractInitBean {

    public OptionStartableBean() {

        configureNode();

        AttributeTypeEntity imeiAttributeType = new AttributeTypeEntity(OptionConstants.IMEI_OPTION_ATTRIBUTE,
                DatatypeType.STRING, true, false);

        this.attributeTypes.add(imeiAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(imeiAttributeType, Locale.ENGLISH
                .getLanguage(), "Imei", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(imeiAttributeType, "nl", "Imei", null));

        AttributeTypeEntity pinAttributeType = new AttributeTypeEntity(OptionConstants.PIN_OPTION_ATTRIBUTE,
                DatatypeType.STRING, false, false);

        this.attributeTypes.add(pinAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinAttributeType, Locale.ENGLISH
                .getLanguage(), "PIN", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(pinAttributeType, "nl", "PIN", null));

        X509Certificate certificate = (X509Certificate) OptionKeyStoreUtils.getPrivateKeyEntry().getCertificate();

        ResourceBundle properties = ResourceBundle.getBundle("config");
        String nodeName = properties.getString("olas.node.name");

        this.devices.add(new Device(OptionConstants.OPTION_DEVICE_ID, SafeOnlineConstants.MOBILE_DEVICE_CLASS,
                nodeName, "/olas-option/auth", "/olas-option/device", "/olas-option/device", null, certificate,
                imeiAttributeType, imeiAttributeType));
        this.deviceDescriptions.add(new DeviceDescription(
				OptionConstants.OPTION_DEVICE_ID, "nl", "Option Datakaart"));
        this.deviceDescriptions.add(new DeviceDescription(OptionConstants.OPTION_DEVICE_ID, Locale.ENGLISH
                .getLanguage(),
				"Option Data Card"));
        this.trustedCertificates.put(certificate, SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN);
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

        this.node = new Node(nodeName, protocol, hostname, hostport, hostportssl, authIdentityServiceClient
                .getCertificate(), identityServiceClient.getCertificate());
        this.trustedCertificates.put(authIdentityServiceClient.getCertificate(),
                SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
    }

    @Override
    public void preStop() {

        this.LOG.debug("pre stop");
    }

    @Override
    public int getPriority() {

        return OptionConstants.OPTION_BOOT_PRIORITY;
    }

}
