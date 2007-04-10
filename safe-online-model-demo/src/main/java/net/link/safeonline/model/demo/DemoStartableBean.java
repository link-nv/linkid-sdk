/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.demo;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.demo.keystore.DemoKeyStoreUtils;
import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStoreUtils;
import net.link.safeonline.demo.ticket.keystore.DemoTicketKeyStoreUtils;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeProviderPK;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.beid.BeIdConstants;

import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "DemoStartableBean")
public class DemoStartableBean extends AbstractInitBean {

	public static final String DEMO_APPLICATION_NAME = "demo-application";

	public static final String DEMO_TICKET_APPLICATION_NAME = "safe-online-demo-ticket";

	public static final String DEMO_PAYMENT_APPLICATION_NAME = "safe-online-demo-payment";

	public static final String DEMO_LAWYER_APPLICATION_NAME = "safe-online-demo-lawyer";

	public DemoStartableBean() {
		configDemoUsers();

		AttributeTypeEntity visaAttributeType = new AttributeTypeEntity(
				"urn:net:lin-k:safe-online:attribute:visaCardNumber", "string",
				true, true);
		this.attributeTypes.add(visaAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				visaAttributeType, Locale.ENGLISH.getLanguage(), "VISA number",
				null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				visaAttributeType, "nl", "VISA-nummer", null));
		this.identities.add(new Identity(DEMO_TICKET_APPLICATION_NAME,
				new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
						BeIdConstants.NRN_ATTRIBUTE, true) }));
		this.identities.add(new Identity(DEMO_PAYMENT_APPLICATION_NAME,
				new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
						visaAttributeType.getName(), true) }));

		PrivateKeyEntry demoPrivateKeyEntry = DemoKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate demoCertificate = (X509Certificate) demoPrivateKeyEntry
				.getCertificate();
		this.registeredApplications.add(new Application(DEMO_APPLICATION_NAME,
				"owner", demoCertificate));

		PrivateKeyEntry demoTicketPrivateKeyEntry = DemoTicketKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate demoTicketCertificate = (X509Certificate) demoTicketPrivateKeyEntry
				.getCertificate();
		this.registeredApplications.add(new Application(
				DEMO_TICKET_APPLICATION_NAME, "owner", demoTicketCertificate));

		PrivateKeyEntry demoPaymentPrivateKeyEntry = DemoPaymentKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate demoPaymentCertificate = (X509Certificate) demoPaymentPrivateKeyEntry
				.getCertificate();
		this.registeredApplications
				.add(new Application(DEMO_PAYMENT_APPLICATION_NAME, "owner",
						demoPaymentCertificate));

		this.trustedCertificates.add(demoCertificate);
		this.trustedCertificates.add(demoTicketCertificate);
		this.trustedCertificates.add(demoPaymentCertificate);

		configLawyerDemo();
	}

	public static final String BAR_ADMIN_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:baradmin";

	private void configLawyerDemo() {
		this.authorizedUsers.put("baradmin", "secret");

		PrivateKeyEntry demoLawyerPrivateKeyEntry = DemoLawyerKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate demoLawyerCertificate = (X509Certificate) demoLawyerPrivateKeyEntry
				.getCertificate();
		this.registeredApplications.add(new Application(
				DEMO_LAWYER_APPLICATION_NAME, "owner", demoLawyerCertificate));
		this.trustedCertificates.add(demoLawyerCertificate);

		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"baradmin", DEMO_LAWYER_APPLICATION_NAME));

		AttributeTypeEntity barAdminType = new AttributeTypeEntity(
				BAR_ADMIN_ATTRIBUTE, "string", true, false);
		this.attributeTypes.add(barAdminType);

		AttributeProviderEntity barAdminProvider = new AttributeProviderEntity();
		barAdminProvider.setPk(new AttributeProviderPK(
				DEMO_LAWYER_APPLICATION_NAME, BAR_ADMIN_ATTRIBUTE));
		this.attributeProviders.add(barAdminProvider);
	}

	private void configDemoUsers() {
		this.authorizedUsers.put("fcorneli", "secret");
		this.authorizedUsers.put("dieter", "secret");
		this.authorizedUsers.put("mario", "secret");

		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"fcorneli", DEMO_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"fcorneli", DEMO_TICKET_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"fcorneli", DEMO_PAYMENT_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, "fcorneli",
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"dieter", DEMO_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"dieter", DEMO_TICKET_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"dieter", DEMO_PAYMENT_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, "dieter",
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"mario", DEMO_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"mario", DEMO_TICKET_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"mario", DEMO_PAYMENT_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, "mario",
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));
	}

	public int getPriority() {
		return BeIdConstants.BEID_BOOT_PRIORITY - 1;
	}
}
