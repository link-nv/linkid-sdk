/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.helpdesk.keystore.HelpdeskKeyStoreUtils;
import net.link.safeonline.oper.keystore.OperKeyStoreUtils;
import net.link.safeonline.owner.keystore.OwnerKeyStoreUtils;
import net.link.safeonline.user.keystore.UserKeyStoreUtils;

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
		configureAttributeTypes();

		this.authorizedUsers.put("admin", "admin");
		this.authorizedUsers.put("owner", "secret");

		this.applicationOwnersAndLogin.put("owner", "owner");

		X509Certificate userCert = (X509Certificate) UserKeyStoreUtils
				.getPrivateKeyEntry().getCertificate();
		X509Certificate operCert = (X509Certificate) OperKeyStoreUtils
				.getPrivateKeyEntry().getCertificate();
		X509Certificate ownerCert = (X509Certificate) OwnerKeyStoreUtils
				.getPrivateKeyEntry().getCertificate();
		X509Certificate helpdeskCert = (X509Certificate) HelpdeskKeyStoreUtils
				.getPrivateKeyEntry().getCertificate();

		this.registeredApplications.add(new Application(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, "owner",
				"The SafeOnline User Web Application.", userCert));
		this.registeredApplications.add(new Application(
				SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME,
				"owner", "The SafeOnline Operator Web Application.", false,
				false, operCert));
		this.registeredApplications.add(new Application(
				SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME,
				"owner", "The SafeOnline Application Owner Web Application.",
				false, false, ownerCert));
		this.registeredApplications.add(new Application(
				SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME,
				"owner", "The SafeOnline Helpdesk Web Application.", false,
				false, helpdeskCert));

		this.trustedCertificates.add(userCert);
		this.trustedCertificates.add(operCert);
		this.trustedCertificates.add(ownerCert);
		this.trustedCertificates.add(helpdeskCert);

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

	private void configureAttributeTypes() {
		AttributeTypeEntity nameAttributeType = new AttributeTypeEntity(
				SafeOnlineConstants.NAME_ATTRIBUTE, DatatypeType.STRING, true,
				true);
		this.attributeTypes.add(nameAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				nameAttributeType, Locale.ENGLISH.getLanguage(), "Name", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				nameAttributeType, "nl", "Naam", null));

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

		List<AttributeTypeEntity> deviceAttributeTypeList = new ArrayList<AttributeTypeEntity>();
		deviceAttributeTypeList.add(passwordHashAttributeType);
		deviceAttributeTypeList.add(passwordSeedAttributeType);
		deviceAttributeTypeList.add(passwordAlgorithmAttributeType);
		this.devices.put("password", deviceAttributeTypeList);
	}

	public int getPriority() {
		return Startable.PRIORITY_BOOTSTRAP;
	}
}
