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
import net.link.safeonline.demo.keystore.DemoKeyStoreUtil;
import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStoreUtils;
import net.link.safeonline.demo.prescription.keystore.DemoPrescriptionKeyStoreUtils;
import net.link.safeonline.demo.ticket.keystore.DemoTicketKeyStoreUtils;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributePK;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeProviderPK;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.beid.BeIdConstants;

import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "DemoStartableBean")
public class DemoStartableBean extends AbstractInitBean {

	public static final String DEMO_APPLICATION_NAME = "demo-application";

	public static final String DEMO_TICKET_APPLICATION_NAME = "demo-ticket";

	public static final String DEMO_PAYMENT_APPLICATION_NAME = "demo-payment";

	public static final String DEMO_LAWYER_APPLICATION_NAME = "demo-lawyer";

	public static final String DEMO_PRESCRIPTION_APPLICATION_NAME = "demo-prescription";

	public DemoStartableBean() {
		configDemoUsers();

		AttributeTypeEntity visaAttributeType = new AttributeTypeEntity(
				DemoConstants.DEMO_VISA_ATTRIBUTE_NAME, DatatypeType.STRING,
				true, true);
		visaAttributeType.setMultivalued(true);
		this.attributeTypes.add(visaAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				visaAttributeType, Locale.ENGLISH.getLanguage(), "VISA number",
				null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				visaAttributeType, "nl", "VISA-nummer", null));
		this.identities.add(new Identity(DEMO_TICKET_APPLICATION_NAME,
				new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
						BeIdConstants.NRN_ATTRIBUTE, true, false) }));
		this.identities.add(new Identity(DEMO_PAYMENT_APPLICATION_NAME,
				new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
						visaAttributeType.getName(), true, false) }));

		PrivateKeyEntry demoPrivateKeyEntry = DemoKeyStoreUtil
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

		configPrescriptionDemo();
	}

	private void configPrescriptionDemo() {
		String prescriptionAdmin = "prescription-admin";
		this.authorizedUsers.put(prescriptionAdmin, "secret");

		/*
		 * Register the prescription demo application within SafeOnline.
		 */
		PrivateKeyEntry demoPrescriptionPrivateKeyEntry = DemoPrescriptionKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate demoPrescriptionCertificate = (X509Certificate) demoPrescriptionPrivateKeyEntry
				.getCertificate();
		this.trustedCertificates.add(demoPrescriptionCertificate);
		this.registeredApplications.add(new Application(
				DEMO_PRESCRIPTION_APPLICATION_NAME, "owner",
				demoPrescriptionCertificate));

		/*
		 * Subscribe the prescription admin.
		 */
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				prescriptionAdmin, DEMO_PRESCRIPTION_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, prescriptionAdmin,
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

		/*
		 * Subscribe the demo users to the prescription demo application.
		 */
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"fcorneli", DEMO_PRESCRIPTION_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"dieter", DEMO_PRESCRIPTION_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"mario", DEMO_PRESCRIPTION_APPLICATION_NAME));

		/*
		 * Attribute Types.
		 */
		configDemoAttribute(DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, DEMO_PRESCRIPTION_APPLICATION_NAME,
				"Prescription Admin", "Voorschriftbeheerder");
		configDemoAttribute(
				DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, DEMO_PRESCRIPTION_APPLICATION_NAME,
				"Care Provider", "Dokter");
		configDemoAttribute(
				DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, DEMO_PRESCRIPTION_APPLICATION_NAME,
				"Pharmacist", "Apotheker");

		/*
		 * Application Identities.
		 */
		this.identities
				.add(new Identity(
						DEMO_PRESCRIPTION_APPLICATION_NAME,
						new IdentityAttributeTypeDO[] {
								new IdentityAttributeTypeDO(
										DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME,
										false, false),
								new IdentityAttributeTypeDO(
										DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME,
										false, false),
								new IdentityAttributeTypeDO(
										DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME,
										false, false) }));

		/*
		 * Also make sure the admin is marked as such.
		 */
		AttributeEntity barAdminBarAdminAttribute = new AttributeEntity();
		barAdminBarAdminAttribute.setPk(new AttributePK(
				DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME,
				prescriptionAdmin));
		barAdminBarAdminAttribute.setBooleanValue(true);
		this.attributes.add(barAdminBarAdminAttribute);
	}

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
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, "baradmin",
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"fcorneli", DEMO_LAWYER_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"dieter", DEMO_LAWYER_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"mario", DEMO_LAWYER_APPLICATION_NAME));

		configLawyerDemoAttribute(
				DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, "Bar administrator", "Baliebeheerder");
		configLawyerDemoAttribute(DemoConstants.LAWYER_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, "Lawyer", "Advocaat");
		configLawyerDemoAttribute(DemoConstants.LAWYER_BAR_ATTRIBUTE_NAME,
				DatatypeType.STRING, "Bar", "Balie");
		configLawyerDemoAttribute(
				DemoConstants.LAWYER_SUSPENDED_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, "Suspended", "Geschorst");

		this.identities.add(new Identity(DEMO_LAWYER_APPLICATION_NAME,
				new IdentityAttributeTypeDO[] {
						new IdentityAttributeTypeDO(
								DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME,
								false, false),
						new IdentityAttributeTypeDO(
								DemoConstants.LAWYER_ATTRIBUTE_NAME, false,
								false),
						new IdentityAttributeTypeDO(
								DemoConstants.LAWYER_BAR_ATTRIBUTE_NAME, false,
								false),
						new IdentityAttributeTypeDO(
								DemoConstants.LAWYER_SUSPENDED_ATTRIBUTE_NAME,
								false, false) }));

		/*
		 * Also make sure the baradmin is marked as such.
		 */
		AttributeEntity barAdminBarAdminAttribute = new AttributeEntity();
		barAdminBarAdminAttribute.setPk(new AttributePK(
				DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME, "baradmin"));
		barAdminBarAdminAttribute.setBooleanValue(true);
		this.attributes.add(barAdminBarAdminAttribute);
	}

	private void configLawyerDemoAttribute(String attributeName,
			DatatypeType datatype, String enName, String nlName) {
		configDemoAttribute(attributeName, datatype,
				DEMO_LAWYER_APPLICATION_NAME, enName, nlName);
	}

	private void configDemoAttribute(String attributeName,
			DatatypeType datatype, String attributeProviderName, String enName,
			String nlName) {
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, datatype, true, false);
		this.attributeTypes.add(attributeType);

		if (null != attributeProviderName) {
			AttributeProviderEntity attributeProvider = new AttributeProviderEntity();
			attributeProvider.setPk(new AttributeProviderPK(
					attributeProviderName, attributeName));
			this.attributeProviders.add(attributeProvider);
		}

		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				attributeType, Locale.ENGLISH.getLanguage(), enName, null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				attributeType, "nl", nlName, null));
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
