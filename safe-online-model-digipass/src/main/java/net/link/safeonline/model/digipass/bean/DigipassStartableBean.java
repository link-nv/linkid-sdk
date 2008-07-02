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
@LocalBinding(jndiBinding = DigipassConstants.DIGIPASS_STARTABLE_JNDI_PREFIX + "DigipassStartableBean")
public class DigipassStartableBean extends AbstractInitBean {

	private static final Log LOG = LogFactory
			.getLog(DigipassStartableBean.class);

	public DigipassStartableBean() {
		configureNode();

		AttributeTypeEntity digipassAttributeType = new AttributeTypeEntity(
				DigipassConstants.DIGIPASS_SN_ATTRIBUTE, DatatypeType.STRING,
				true, false, true);
		digipassAttributeType.setMultivalued(true);
		this.attributeTypes.add(digipassAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				digipassAttributeType, Locale.ENGLISH.getLanguage(),
				"Digipass Serial number", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				digipassAttributeType, "nl", "Digipass Serie nummer", null));

		X509Certificate certificate = (X509Certificate) DigipassKeyStoreUtils
				.getPrivateKeyEntry().getCertificate();

		ResourceBundle properties = ResourceBundle.getBundle("config");
		String nodeName = properties.getString("olas.node.name");

		this.devices.add(new Device(DigipassConstants.DIGIPASS_DEVICE_ID,
				SafeOnlineConstants.DIGIPASS_DEVICE_CLASS, nodeName,
				"/olas-digipass/auth", null, null, null, certificate,
				digipassAttributeType, digipassAttributeType));
		this.deviceDescriptions.add(new DeviceDescription(
				DigipassConstants.DIGIPASS_DEVICE_ID, "nl", "Digipass Bank X"));
		this.deviceDescriptions.add(new DeviceDescription(
				DigipassConstants.DIGIPASS_DEVICE_ID, Locale.ENGLISH
						.getLanguage(), "Digipass Bank X"));
		this.trustedCertificates.put(certificate,
				SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN);
		/*
		 * WS-Notification subscriptions
		 */
		configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, certificate);
	}

	private void configSubscription(String topic, X509Certificate certificate) {
		ResourceBundle properties = ResourceBundle.getBundle("config");
		String protocol = properties.getString("olas.host.protocol");
		String hostname = properties.getString("olas.host.name");
		int hostport = Integer.parseInt(properties.getString("olas.host.port"));
		int hostportssl = Integer.parseInt(properties
				.getString("olas.host.port.ssl"));
		String address = protocol + "://" + hostname + ":";
		if (protocol.equals("http"))
			address += hostport;
		else
			address += hostportssl;
		address += "/safe-online-ws/consumer";
		this.notificationSubcriptions.add(new NotificationSubscription(topic,
				address, certificate));
	}

	private void configureNode() {
		ResourceBundle properties = ResourceBundle.getBundle("config");
		String nodeName = properties.getString("olas.node.name");
		String protocol = properties.getString("olas.host.protocol");
		String hostname = properties.getString("olas.host.name");
		int hostport = Integer.parseInt(properties.getString("olas.host.port"));
		int hostportssl = Integer.parseInt(properties
				.getString("olas.host.port.ssl"));

		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
		IdentityServiceClient identityServiceClient = new IdentityServiceClient();

		this.node = new Node(nodeName, protocol, hostname, hostport,
				hostportssl, authIdentityServiceClient.getCertificate(),
				identityServiceClient.getCertificate());
		this.trustedCertificates.put(
				authIdentityServiceClient.getCertificate(),
				SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
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
