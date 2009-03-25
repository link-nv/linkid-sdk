/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.demo.bean;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.demo.bank.keystore.DemoBankKeyStore;
import net.link.safeonline.demo.cinema.keystore.DemoCinemaKeyStore;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStore;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.model.digipass.DigipassConstants;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.model.password.PasswordManager;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.servlet.SafeOnlineConfig;

import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = DemoStartableBean.JNDI_BINDING)
public class DemoStartableBean extends AbstractInitBean {

	public static final String JNDI_BINDING = DemoConstants.DEMO_STARTABLE_JNDI_PREFIX
			+ "DemoStartableBean";

	private static final String PASSWORD = "secret";

	public static final String LICENSE_AGREEMENT_CONFIRM_TEXT_EN = "PLEASE READ THIS SOFTWARE LICENSE AGREEMENT (\"LICENSE\") CAREFULLY BEFORE USING THE SOFTWARE. \n BY USING THE SOFTWARE, YOU ARE AGREEING TO BE BOUND BY THE TERMS OF THIS LICENSE. \n IF YOU ARE ACCESSING THE SOFTWARE ELECTRONICALLY, SIGNIFY YOUR AGREEMENT TO BE BOUND BY THE TERMS OF THIS LICENSE BY CLICKING THE \"AGREE/ACCEPT\" BUTTON. \n IF YOU DO NOT AGREE TO THE TERMS OF THIS LICENSE, DO NOT USE THE SOFTWARE AND (IF APPLICABLE) RETURN THE APPLE SOFTWARE TO THE PLACE WHERE YOU OBTAINED IT FOR A REFUND OR, IF THE SOFTWARE WAS ACCESSED ELECTRONICALLY, CLICK \"DISAGREE/DECLINE\".";

	public static final String LICENSE_AGREEMENT_CONFIRM_TEXT_NL = "GELIEVE ZORGVULDIG DEZE OVEREENKOMST VAN DE VERGUNNING VAN SOFTWARE (\"LICENSE \") TE LEZEN ALVORENS DE SOFTWARE TE GEBRUIKEN.";

	private static class PasswordRegistration {

		final String login;

		final String password;

		public PasswordRegistration(String login, String password) {

			this.login = login;
			this.password = password;
		}
	}

	private List<PasswordRegistration> passwordRegistrations;

	private String demoPaymentWebappName;
	private String demoCinemaWebappName;
	private String demoBankWebappName;

	private String demoPaymentWebappUrl;
	private String demoCinemaWebappUrl;
	private String demoBankWebappUrl;

	@EJB(mappedName = PasswordManager.JNDI_BINDING)
	private PasswordManager passwordManager;

	@EJB(mappedName = SubjectService.JNDI_BINDING)
	private SubjectService subjectService;

	@Override
	public void postStart() {

		ResourceBundle properties = ResourceBundle.getBundle("config");
		demoPaymentWebappName = properties
				.getString("olas.demo.payment.webapp.name");
		demoCinemaWebappName = properties
				.getString("olas.demo.cinema.webapp.name");
		demoBankWebappName = properties.getString("olas.demo.bank.webapp.name");
		demoPaymentWebappUrl = properties
				.getString("olas.demo.payment.webapp.url");
		demoCinemaWebappUrl = properties
				.getString("olas.demo.cinema.webapp.url");
		demoBankWebappUrl = properties.getString("olas.demo.bank.webapp.url");
		passwordRegistrations = new LinkedList<PasswordRegistration>();

		configureNode();

		configBankDemo();

		configCinemaDemo();

		configPaymentDemo();

		super.postStart();

		for (PasswordRegistration passwordRegistration : passwordRegistrations) {

			SubjectEntity subject = subjectService
					.findSubjectFromUserName(passwordRegistration.login);
			if (null == subject) {
				try {
					subject = subjectService
							.addSubject(passwordRegistration.login);
				} catch (AttributeTypeNotFoundException e) {
					LOG.fatal("safeonline exception", e);
					throw new EJBException(e);
				}
			}

			if (!passwordManager.isPasswordConfigured(subject)) {
				passwordManager.registerPassword(subject,
						passwordRegistration.password);
			}
		}

	}

	private void configBankDemo() {

		PrivateKeyEntry demoBankPrivateKeyEntry = DemoBankKeyStore
				.getPrivateKeyEntry();
		X509Certificate demoBankCertificate = (X509Certificate) demoBankPrivateKeyEntry
				.getCertificate();

		trustedCertificates.put(demoBankCertificate,
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
		try {
			URL demoBankApplicationUrl = new URL(SafeOnlineConfig
					.nodeProtocol(), SafeOnlineConfig.nodeHost(),
					SafeOnlineConfig.nodePort(), "/" + demoBankWebappUrl);
			URL demoBankSSOLogoutUrl = new URL(SafeOnlineConfig
					.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
					SafeOnlineConfig.nodePortSecure(), "/" + demoBankWebappUrl
							+ "/logout");

			registeredApplications.add(new Application(demoBankWebappName,
					"owner", null, demoBankApplicationUrl,
					getLogo("/ebank-small.png"), true, true,
					demoBankCertificate, false, IdScopeType.SUBSCRIPTION, true,
					demoBankSSOLogoutUrl));
		} catch (MalformedURLException e) {
			throw new EJBException("Malformed URL Exception: " + e.getMessage());
		}

		identities
				.add(new Identity(
						demoBankWebappName,
						new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
								DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true,
								false) }));

		List<String> tempAllowedDevices = new LinkedList<String>();
		tempAllowedDevices.add(BeIdConstants.BEID_DEVICE_ID);
		tempAllowedDevices.add(EncapConstants.ENCAP_DEVICE_ID);
		allowedDevices.put(demoBankWebappName, tempAllowedDevices);

		/*
		 * Application usage agreements
		 */
		UsageAgreement usageAgreement = new UsageAgreement(demoBankWebappName);
		usageAgreement.addUsageAgreementText(new UsageAgreementText(
				Locale.ENGLISH.getLanguage(), "English" + "\n\n" + "Lin-k NV"
						+ "\n" + "Software License Agreement for "
						+ demoBankWebappName + "\n\n"
						+ LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
		usageAgreement.addUsageAgreementText(new UsageAgreementText("nl",
				"Nederlands" + "\n\n" + "Lin-k NV" + "\n"
						+ "Software Gebruikers Overeenkomst voor "
						+ demoBankWebappName + "\n\n"
						+ LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
		usageAgreements.add(usageAgreement);

		/*
		 * WS-Notification subscriptions
		 */
		configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER,
				demoBankCertificate);
		configSubscription(SafeOnlineConstants.TOPIC_UNSUBSCRIBE_USER,
				demoBankCertificate);
	}

	private void configCinemaDemo() {

		PrivateKeyEntry demoCinemaPrivateKeyEntry = DemoCinemaKeyStore
				.getPrivateKeyEntry();
		X509Certificate demoCinemaCertificate = (X509Certificate) demoCinemaPrivateKeyEntry
				.getCertificate();

		trustedCertificates.put(demoCinemaCertificate,
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
		try {
			URL demoCinemaApplicationUrl = new URL(SafeOnlineConfig
					.nodeProtocol(), SafeOnlineConfig.nodeHost(),
					SafeOnlineConfig.nodePort(), "/" + demoCinemaWebappUrl);
			URL demoCinemaSSOLogoutUrl = new URL(SafeOnlineConfig
					.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
					SafeOnlineConfig.nodePortSecure(), "/"
							+ demoCinemaWebappUrl + "/logout");

			registeredApplications.add(new Application(demoCinemaWebappName,
					"owner", null, demoCinemaApplicationUrl,
					getLogo("/ecinema-small.png"), true, true,
					demoCinemaCertificate, false, IdScopeType.SUBSCRIPTION,
					true, demoCinemaSSOLogoutUrl));
		} catch (MalformedURLException e) {
			throw new EJBException("Malformed URL Exception: " + e.getMessage());
		}

		identities
				.add(new Identity(
						demoCinemaWebappName,
						new IdentityAttributeTypeDO[] {
								new IdentityAttributeTypeDO(
										BeIdConstants.BEID_NRN_ATTRIBUTE,
										false, false),
								new IdentityAttributeTypeDO(
										DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME,
										false, false),
								new IdentityAttributeTypeDO(
										DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME,
										true, false) }));

		// Uncomment this to restrict cinema access through BeID device.
		List<String> tempAllowedDevices = new LinkedList<String>();
		tempAllowedDevices.add(BeIdConstants.BEID_DEVICE_ID);
		tempAllowedDevices.add(EncapConstants.ENCAP_DEVICE_ID);
		allowedDevices.put(demoCinemaWebappName, tempAllowedDevices);

		/*
		 * Application usage agreements
		 */
		UsageAgreement usageAgreement = new UsageAgreement(demoCinemaWebappName);
		usageAgreement.addUsageAgreementText(new UsageAgreementText(
				Locale.ENGLISH.getLanguage(), "English" + "\n\n" + "Lin-k NV"
						+ "\n" + "Software License Agreement for "
						+ demoCinemaWebappName + "\n\n"
						+ LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
		usageAgreement.addUsageAgreementText(new UsageAgreementText("nl",
				"Nederlands" + "\n\n" + "Lin-k NV" + "\n"
						+ "Software Gebruikers Overeenkomst voor "
						+ demoCinemaWebappName + "\n\n"
						+ LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
		usageAgreements.add(usageAgreement);

		/*
		 * WS-Notification subscriptions
		 */
		configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER,
				demoCinemaCertificate);
	}

	private void configPaymentDemo() {

		String paymentAdmin = "payment-admin";
		users.add(paymentAdmin);
		passwordRegistrations.add(new PasswordRegistration(paymentAdmin,
				PASSWORD));

		/*
		 * Register the payment and ticket demo application within SafeOnline.
		 */
		PrivateKeyEntry demoPaymentPrivateKeyEntry = DemoPaymentKeyStore
				.getPrivateKeyEntry();
		X509Certificate demoPaymentCertificate = (X509Certificate) demoPaymentPrivateKeyEntry
				.getCertificate();

		try {
			URL demoPaymentApplicationUrl = new URL(SafeOnlineConfig
					.nodeProtocol(), SafeOnlineConfig.nodeHost(),
					SafeOnlineConfig.nodePort(), "/" + demoPaymentWebappUrl);
			URL demoPaymentSSOLogoutUrl = new URL(SafeOnlineConfig
					.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
					SafeOnlineConfig.nodePortSecure(), "/"
							+ demoPaymentWebappUrl + "/logout");

			registeredApplications.add(new Application(demoPaymentWebappName,
					"owner", null, demoPaymentApplicationUrl,
					getLogo("/epayment-small.png"), true, true,
					demoPaymentCertificate, true, IdScopeType.SUBSCRIPTION,
					true, demoPaymentSSOLogoutUrl));
		} catch (MalformedURLException e) {
			throw new EJBException("Malformed URL Exception: " + e.getMessage());
		}

		trustedCertificates.put(demoPaymentCertificate,
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);

		/*
		 * Subscribe the payment admin.
		 */
		subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				paymentAdmin, demoPaymentWebappName));
		subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION,
				paymentAdmin,
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

		/*
		 * Attribute Types.
		 */
		configDemoAttribute(DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME,
				DatatypeType.BOOLEAN, false, demoPaymentWebappName,
				"Junior Account", "Jongerenrekening", true, false);
		configDemoAttribute(DemoConstants.DEMO_VISA_ATTRIBUTE_NAME,
				DatatypeType.STRING, true, null, "VISA number", "VISA nummer",
				true, true);

		/*
		 * Application Identities.
		 */
		identities.add(new Identity(demoPaymentWebappName,
				new IdentityAttributeTypeDO[] {
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
		 * device restriction
		 */
		List<String> tempAllowedDevices = new LinkedList<String>();
		tempAllowedDevices.add(BeIdConstants.BEID_DEVICE_ID);
		tempAllowedDevices.add(DigipassConstants.DIGIPASS_DEVICE_ID);
		tempAllowedDevices.add(EncapConstants.ENCAP_DEVICE_ID);
		allowedDevices.put(demoPaymentWebappName, tempAllowedDevices);

		/*
		 * Application usage agreements
		 */
		UsageAgreement usageAgreement = new UsageAgreement(
				demoPaymentWebappName);
		usageAgreement.addUsageAgreementText(new UsageAgreementText(
				Locale.ENGLISH.getLanguage(), "English" + "\n\n" + "Lin-k NV"
						+ "\n" + "Software License Agreement for "
						+ demoPaymentWebappName + "\n\n"
						+ LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
		usageAgreement.addUsageAgreementText(new UsageAgreementText("nl",
				"Nederlands" + "\n\n" + "Lin-k NV" + "\n"
						+ "Software Gebruikers Overeenkomst voor "
						+ demoPaymentWebappName + "\n\n"
						+ LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
		usageAgreements.add(usageAgreement);

		/*
		 * WS-Notification subscriptions
		 */
		configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER,
				demoPaymentCertificate);

	}

	private AttributeTypeEntity configDemoAttribute(String attributeName,
			DatatypeType datatype, boolean multiValued,
			String attributeProviderName, String enName, String nlName,
			boolean userVisible, boolean userEditable) {

		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, datatype, userVisible, userEditable);
		attributeType.setMultivalued(multiValued);
		attributeTypes.add(attributeType);

		if (null != attributeProviderName) {
			attributeProviders.add(new AttributeProvider(attributeProviderName,
					attributeName));
		}

		if (null != enName) {
			attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
					attributeType, Locale.ENGLISH.getLanguage(), enName, null));
		}
		if (null != nlName) {
			attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
					attributeType, "nl", nlName, null));
		}
		return attributeType;
	}

	private void configSubscription(String topic, X509Certificate certificate) {

		String address = String.format("%s://%s:%d/%s", SafeOnlineConfig
				.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
				SafeOnlineConfig.nodePortSecure(),
				"safe-online-demo-ws/consumer");

		notificationSubcriptions.add(new NotificationSubscription(topic,
				address, certificate));
	}

	private void configureNode() {

		SafeOnlineNodeKeyStore nodeKeyStore = new SafeOnlineNodeKeyStore();

		node = new Node(SafeOnlineConfig.nodeName(), SafeOnlineConfig
				.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
				SafeOnlineConfig.nodePort(), SafeOnlineConfig.nodePortSecure(),
				nodeKeyStore.getCertificate());
		trustedCertificates.put(nodeKeyStore.getCertificate(),
				SafeOnlineConstants.SAFE_ONLINE_NODE_TRUST_DOMAIN);
	}

	@Override
	public int getPriority() {

		return BeIdConstants.BEID_BOOT_PRIORITY - 1;
	}
}
