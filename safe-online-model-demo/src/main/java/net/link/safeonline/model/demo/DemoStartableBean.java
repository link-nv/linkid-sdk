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
import net.link.safeonline.demo.mandate.keystore.DemoMandateKeyStoreUtils;
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

	public static final String DEMO_MANDATE_APPLICATION_NAME = "demo-mandate";

	public DemoStartableBean() {
		configDemoUsers();

		PrivateKeyEntry demoPrivateKeyEntry = DemoKeyStoreUtil
				.getPrivateKeyEntry();
		X509Certificate demoCertificate = (X509Certificate) demoPrivateKeyEntry
				.getCertificate();
		this.registeredApplications.add(new Application(DEMO_APPLICATION_NAME,
				"owner", demoCertificate));

		this.trustedCertificates.add(demoCertificate);

		configTicketDemo();

		configPaymentDemo();

		configLawyerDemo();

		configPrescriptionDemo();

		configMandateDemo();
	}

	private void configMandateDemo() {
		PrivateKeyEntry demoMandatePrivateKeyEntry = DemoMandateKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate demoMandateCertificate = (X509Certificate) demoMandatePrivateKeyEntry
				.getCertificate();

		/*
		 * Register the application and the application certificate.
		 */
		this.trustedCertificates.add(demoMandateCertificate);
		this.registeredApplications
				.add(new Application(DEMO_MANDATE_APPLICATION_NAME, "owner",
						demoMandateCertificate));

		/*
		 * Subscribe the demo users to the mandate demo application.
		 */
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"fcorneli", DEMO_MANDATE_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"dieter", DEMO_MANDATE_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"mario", DEMO_MANDATE_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"wvdhaute", DEMO_MANDATE_APPLICATION_NAME));

		/*
		 * Register mandate attribute type
		 */
		AttributeTypeEntity mandateCompanyAttributeType = configDemoAttribute(
				DemoConstants.MANDATE_COMPANY_ATTRIBUTE_NAME,
				DatatypeType.STRING, true, null, "Company", "Bedrijf", true,
				false);
		AttributeTypeEntity mandateTitleAttributeType = configDemoAttribute(
				DemoConstants.MANDATE_TITLE_ATTRIBUTE_NAME,
				DatatypeType.STRING, true, null, "Title", "Titel", true, false);

		AttributeTypeEntity mandateAttributeType = new AttributeTypeEntity(
				DemoConstants.MANDATE_ATTRIBUTE_NAME, DatatypeType.COMPOUNDED,
				true, false);
		mandateAttributeType.setMultivalued(true);
		mandateAttributeType.addMember(mandateCompanyAttributeType, 0, true);
		mandateAttributeType.addMember(mandateTitleAttributeType, 1, true);
		this.attributeTypes.add(mandateAttributeType);

		AttributeProviderEntity attributeProvider = new AttributeProviderEntity();
		attributeProvider.setPk(new AttributeProviderPK(
				DEMO_MANDATE_APPLICATION_NAME,
				DemoConstants.MANDATE_ATTRIBUTE_NAME));
		this.attributeProviders.add(attributeProvider);

		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				mandateAttributeType, Locale.ENGLISH.getLanguage(), "Mandate",
				null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				mandateAttributeType, "nl", "Mandaat", null));

		/*
		 * Application Identities
		 */
		this.identities
				.add(new Identity(
						DEMO_MANDATE_APPLICATION_NAME,
						new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
								DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true,
								false) }));

		/*
		 * Register admin
		 */
		String mandateAdmin = "mandate-admin";
		this.authorizedUsers.put(mandateAdmin, "secret");

		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				mandateAdmin, DEMO_MANDATE_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, mandateAdmin,
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

	}

	private void configTicketDemo() {
		PrivateKeyEntry demoTicketPrivateKeyEntry = DemoTicketKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate demoTicketCertificate = (X509Certificate) demoTicketPrivateKeyEntry
				.getCertificate();

		this.trustedCertificates.add(demoTicketCertificate);
		this.registeredApplications.add(new Application(
				DEMO_TICKET_APPLICATION_NAME, "owner", demoTicketCertificate));

		this.identities.add(new Identity(DEMO_TICKET_APPLICATION_NAME,
				new IdentityAttributeTypeDO[] {
						new IdentityAttributeTypeDO(
								BeIdConstants.NRN_ATTRIBUTE, true, false),
						new IdentityAttributeTypeDO(
								DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME,
								false, false),
						new IdentityAttributeTypeDO(
								DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true,
								false) }));
	}

	private void configPaymentDemo() {

		String paymentAdmin = "payment-admin";
		this.authorizedUsers.put(paymentAdmin, "secret");

		/*
		 * Register the payment and ticket demo application within SafeOnline.
		 */
		PrivateKeyEntry demoPaymentPrivateKeyEntry = DemoPaymentKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate demoPaymentCertificate = (X509Certificate) demoPaymentPrivateKeyEntry
				.getCertificate();

		this.trustedCertificates.add(demoPaymentCertificate);

		this.registeredApplications
				.add(new Application(DEMO_PAYMENT_APPLICATION_NAME, "owner",
						demoPaymentCertificate));

		/*
		 * Subscribe the payment admin.
		 */
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				paymentAdmin, DEMO_PAYMENT_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, paymentAdmin,
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

		/*
		 * Subscribe the demo users to the payment demo application.
		 */
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"fcorneli", DEMO_PAYMENT_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"dieter", DEMO_PAYMENT_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"mario", DEMO_PAYMENT_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"wvdhaute", DEMO_PAYMENT_APPLICATION_NAME));

		/*
		 * Attribute Types.
		 */
		configDemoAttribute(DemoConstants.PAYMENT_ADMIN_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, false, DEMO_PAYMENT_APPLICATION_NAME,
				"Payment Admin", "Rekeningbeheerder", true, false);
		configDemoAttribute(DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, false, DEMO_PAYMENT_APPLICATION_NAME,
				"Junior Account", "Jongerenrekening", true, false);
		configDemoAttribute(DemoConstants.DEMO_VISA_ATTRIBUTE_NAME,
				DatatypeType.STRING, true, null, "VISA number", "VISA nummer",
				true, true);

		/*
		 * Application Identities.
		 */
		this.identities.add(new Identity(DEMO_PAYMENT_APPLICATION_NAME,
				new IdentityAttributeTypeDO[] {
						new IdentityAttributeTypeDO(
								DemoConstants.PAYMENT_ADMIN_ATTRIBUTE_NAME,
								false, false),
						new IdentityAttributeTypeDO(
								DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME,
								false, false),
						new IdentityAttributeTypeDO(
								DemoConstants.DEMO_VISA_ATTRIBUTE_NAME, true,
								false),
						new IdentityAttributeTypeDO(
								DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true,
								false) }));

		/*
		 * Also make sure the admin is marked as such.
		 */
		AttributeEntity paymentAdminAttribute = new AttributeEntity();
		paymentAdminAttribute.setPk(new AttributePK(
				DemoConstants.PAYMENT_ADMIN_ATTRIBUTE_NAME, paymentAdmin));
		paymentAdminAttribute.setBooleanValue(true);
		this.attributes.add(paymentAdminAttribute);
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
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"wvdhaute", DEMO_PRESCRIPTION_APPLICATION_NAME));

		/*
		 * Attribute Types.
		 */
		configDemoAttribute(DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, false,
				DEMO_PRESCRIPTION_APPLICATION_NAME, "Prescription Admin",
				"Voorschriftbeheerder", true, false);
		configDemoAttribute(
				DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, false,
				DEMO_PRESCRIPTION_APPLICATION_NAME, "Care Provider", "Dokter",
				true, false);
		configDemoAttribute(
				DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, false,
				DEMO_PRESCRIPTION_APPLICATION_NAME, "Pharmacist", "Apotheker",
				true, false);

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
										false, false),
								new IdentityAttributeTypeDO(
										DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME,
										true, false) }));

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
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"wvdhaute", DEMO_LAWYER_APPLICATION_NAME));

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
								false, false),
						new IdentityAttributeTypeDO(
								DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true,
								false) }));

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
		configDemoAttribute(attributeName, datatype, false,
				DEMO_LAWYER_APPLICATION_NAME, enName, nlName, true, false);
	}

	private AttributeTypeEntity configDemoAttribute(String attributeName,
			DatatypeType datatype, boolean multiValued,
			String attributeProviderName, String enName, String nlName,
			boolean userVisible, boolean userEditable) {
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, datatype, userVisible, userEditable);
		attributeType.setMultivalued(multiValued);
		this.attributeTypes.add(attributeType);

		if (null != attributeProviderName) {
			AttributeProviderEntity attributeProvider = new AttributeProviderEntity();
			attributeProvider.setPk(new AttributeProviderPK(
					attributeProviderName, attributeName));
			this.attributeProviders.add(attributeProvider);
		}

		if (null != enName) {
			this.attributeTypeDescriptions
					.add(new AttributeTypeDescriptionEntity(attributeType,
							Locale.ENGLISH.getLanguage(), enName, null));
		}
		if (null != nlName) {
			this.attributeTypeDescriptions
					.add(new AttributeTypeDescriptionEntity(attributeType,
							"nl", nlName, null));
		}
		return attributeType;
	}

	private void configDemoUsers() {
		this.authorizedUsers.put("fcorneli", "secret");
		this.authorizedUsers.put("dieter", "secret");
		this.authorizedUsers.put("mario", "secret");
		this.authorizedUsers.put("wvdhaute", "secret");

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

		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"wvdhaute", DEMO_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"wvdhaute", DEMO_TICKET_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"wvdhaute", DEMO_PAYMENT_APPLICATION_NAME));
		this.subscriptions.add(new Subscription(
				SubscriptionOwnerType.APPLICATION, "wvdhaute",
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

	}

	@Override
	public int getPriority() {
		return BeIdConstants.BEID_BOOT_PRIORITY - 1;
	}
}
