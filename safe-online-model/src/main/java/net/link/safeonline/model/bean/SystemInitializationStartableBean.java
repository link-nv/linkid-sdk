/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

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
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.helpdesk.keystore.HelpdeskKeyStoreUtils;
import net.link.safeonline.oper.keystore.OperKeyStoreUtils;
import net.link.safeonline.owner.keystore.OwnerKeyStoreUtils;
import net.link.safeonline.user.keystore.UserKeyStoreUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * This component will initialize the system at startup.
 * 
 * For now it creates initial users, applications and subscriptions. This to
 * allow for admins to gain access to the system and thus to bootstrap the
 * SafeOnline core.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX
		+ "SystemInitializationStartableBean")
public class SystemInitializationStartableBean extends AbstractInitBean {

	public SystemInitializationStartableBean() {
		configureNode();
		configureAttributeTypes();
		configureDevices();

		this.authorizedUsers.put("admin", new AuthenticationDevice("admin",
				null, null));
		this.authorizedUsers.put("owner", new AuthenticationDevice("secret",
				null, null));

		this.applicationOwnersAndLogin.put("owner", "owner");

		X509Certificate userCert = (X509Certificate) UserKeyStoreUtils
				.getPrivateKeyEntry().getCertificate();
		X509Certificate operCert = (X509Certificate) OperKeyStoreUtils
				.getPrivateKeyEntry().getCertificate();
		X509Certificate ownerCert = (X509Certificate) OwnerKeyStoreUtils
				.getPrivateKeyEntry().getCertificate();
		X509Certificate helpdeskCert = (X509Certificate) HelpdeskKeyStoreUtils
				.getPrivateKeyEntry().getCertificate();

		// TODO: Fill in the correct Home URL and Logo URL.
		this.registeredApplications.add(new Application(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, "owner",
				"The SafeOnline User Web Application.", null, null, null,
				false, false, userCert, false, IdScopeType.USER));
		this.registeredApplications.add(new Application(
				SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME,
				"owner", "The SafeOnline Operator Web Application.", null,
				null, null, false, false, operCert, false, IdScopeType.USER));
		this.registeredApplications.add(new Application(
				SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME,
				"owner", "The SafeOnline Application Owner Web Application.",
				null, null, null, false, false, ownerCert, false,
				IdScopeType.USER));
		this.registeredApplications
				.add(new Application(
						SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME,
						"owner", "The SafeOnline Helpdesk Web Application.",
						null, null, null, false, false, helpdeskCert, false,
						IdScopeType.USER));

		this.trustedCertificates.put(userCert,
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
		this.trustedCertificates.put(operCert,
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
		this.trustedCertificates.put(ownerCert,
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
		this.trustedCertificates.put(helpdeskCert,
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);

		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, "admin",
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, "admin",
				SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, "admin",
				SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME));

		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, "owner",
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, "owner",
				SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME));
	}

	private void configureNode() {
		ResourceBundle properties = ResourceBundle.getBundle("config");
		String nodeName = properties.getString("olas.node.name");
		String hostname = properties.getString("olas.host.name");
		int hostport = Integer.parseInt(properties.getString("olas.host.port"));
		int hostportssl = Integer.parseInt(properties
				.getString("olas.host.port.ssl"));

		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
		IdentityServiceClient identityServiceClient = new IdentityServiceClient();
		this.node = new Node(nodeName, hostname, hostport, hostportssl,
				authIdentityServiceClient.getCertificate(),
				identityServiceClient.getCertificate());
		this.trustedCertificates.put(
				authIdentityServiceClient.getCertificate(),
				SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
		this.trustedCertificates.put(identityServiceClient.getCertificate(),
				SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
	}

	private void configureAttributeTypes() {
		AttributeTypeEntity nameAttributeType = new AttributeTypeEntity(
				SafeOnlineConstants.NAME_ATTRIBUTE, DatatypeType.STRING, true,
				true);
		this.attributeTypes.add(nameAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				nameAttributeType, Locale.ENGLISH.getLanguage(), "Name", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				nameAttributeType, "nl", "Naam", null));

		AttributeTypeEntity loginAttributeType = new AttributeTypeEntity(
				SafeOnlineConstants.LOGIN_ATTRIBTUE, DatatypeType.LOGIN, false,
				false);
		this.attributeTypes.add(loginAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				loginAttributeType, Locale.ENGLISH.getLanguage(), "Login name",
				null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				loginAttributeType, "nl", "Login naam", null));
	}

	private void configureDevices() {
		this.deviceClasses.add(new DeviceClass(
				SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
				SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS));
		this.deviceClasses.add(new DeviceClass(
				SafeOnlineConstants.MOBILE_DEVICE_CLASS,
				SafeOnlineConstants.MOBILE_DEVICE_AUTH_CONTEXT_CLASS));
		this.deviceClasses.add(new DeviceClass(
				SafeOnlineConstants.PKI_DEVICE_CLASS,
				SafeOnlineConstants.PKI_DEVICE_AUTH_CONTEXT_CLASS));

		configurePasswordDevice();
	}

	private void configurePasswordDevice() {
		AttributeTypeEntity passwordHashAttributeType = new AttributeTypeEntity(
				SafeOnlineConstants.PASSWORD_HASH_ATTRIBUTE,
				DatatypeType.STRING, false, false);
		AttributeTypeEntity passwordSeedAttributeType = new AttributeTypeEntity(
				SafeOnlineConstants.PASSWORD_SEED_ATTRIBUTE,
				DatatypeType.STRING, false, false);
		AttributeTypeEntity passwordAlgorithmAttributeType = new AttributeTypeEntity(
				SafeOnlineConstants.PASSWORD_ALGORITHM_ATTRIBUTE,
				DatatypeType.STRING, false, false);
		this.attributeTypes.add(passwordHashAttributeType);
		this.attributeTypes.add(passwordSeedAttributeType);
		this.attributeTypes.add(passwordAlgorithmAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				passwordHashAttributeType, Locale.ENGLISH.getLanguage(),
				"Password hash", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				passwordSeedAttributeType, Locale.ENGLISH.getLanguage(),
				"Password hash seed", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				passwordAlgorithmAttributeType, Locale.ENGLISH.getLanguage(),
				"Password hash algorithm", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				passwordHashAttributeType, "nl", "Wachtwoord hash", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				passwordSeedAttributeType, "nl", "Wachtwoord hash seed", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				passwordAlgorithmAttributeType, "nl",
				"Wachtwoord hash algoritme", null));

		AttributeTypeEntity passwordDeviceAttributeType = new AttributeTypeEntity(
				SafeOnlineConstants.PASSWORD_DEVICE_ATTRIBUTE,
				DatatypeType.COMPOUNDED, false, false);
		passwordDeviceAttributeType.setMultivalued(true);
		passwordDeviceAttributeType.addMember(passwordHashAttributeType, 0,
				true);
		passwordDeviceAttributeType.addMember(passwordSeedAttributeType, 1,
				true);
		passwordDeviceAttributeType.addMember(passwordAlgorithmAttributeType,
				2, true);
		this.attributeTypes.add(passwordDeviceAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				passwordDeviceAttributeType, Locale.ENGLISH.getLanguage(),
				"Password", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				passwordDeviceAttributeType, "nl", "Wachtwoord", null));

		this.devices.add(new Device(
				SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID,
				SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
				"password/username-password.seam",
				"password/register-password.seam",
				"password/remove-password.seam",
				"password/register-password.seam", null,
				passwordDeviceAttributeType));
		this.deviceDescriptions.add(new DeviceDescription(
				SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, "nl",
				"Paswoord"));
		this.deviceDescriptions.add(new DeviceDescription(
				SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, Locale.ENGLISH
						.getLanguage(), "Password"));
	}

	@Override
	public int getPriority() {
		return Startable.PRIORITY_BOOTSTRAP;
	}
}
