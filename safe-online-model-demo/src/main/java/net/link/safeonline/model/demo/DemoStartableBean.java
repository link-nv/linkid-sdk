/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.demo;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.demo.bank.keystore.DemoBankKeyStoreUtils;
import net.link.safeonline.demo.cinema.keystore.DemoCinemaKeyStoreUtils;
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
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.digipass.DigipassConstants;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.model.option.OptionConstants;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = DemoConstants.DEMO_STARTABLE_JNDI_PREFIX + "DemoStartableBean")
public class DemoStartableBean extends AbstractInitBean {

    public static final String  DEMO_APPLICATION_NAME              = "demo-application";

    public static final String  DEMO_TICKET_APPLICATION_NAME       = "demo-ticket";

    public static final String  DEMO_PAYMENT_APPLICATION_NAME      = "ebank";

    public static final String  DEMO_LAWYER_APPLICATION_NAME       = "demo-lawyer";

    public static final String  DEMO_PRESCRIPTION_APPLICATION_NAME = "demo-prescription";

    public static final String  DEMO_MANDATE_APPLICATION_NAME      = "demo-mandate";

    private static final String DEMO_CINEMA_APPLICATION_NAME       = "cinema";

    private static final String DEMO_BANK_APPLICATION_NAME         = "demo-bank";

    public static final String  LICENSE_AGREEMENT_CONFIRM_TEXT_EN  = "PLEASE READ THIS SOFTWARE LICENSE AGREEMENT (\"LICENSE\") CAREFULLY BEFORE USING THE SOFTWARE. \n BY USING THE SOFTWARE, YOU ARE AGREEING TO BE BOUND BY THE TERMS OF THIS LICENSE. \n IF YOU ARE ACCESSING THE SOFTWARE ELECTRONICALLY, SIGNIFY YOUR AGREEMENT TO BE BOUND BY THE TERMS OF THIS LICENSE BY CLICKING THE \"AGREE/ACCEPT\" BUTTON. \n IF YOU DO NOT AGREE TO THE TERMS OF THIS LICENSE, DO NOT USE THE SOFTWARE AND (IF APPLICABLE) RETURN THE APPLE SOFTWARE TO THE PLACE WHERE YOU OBTAINED IT FOR A REFUND OR, IF THE SOFTWARE WAS ACCESSED ELECTRONICALLY, CLICK \"DISAGREE/DECLINE\".";

    public static final String  LICENSE_AGREEMENT_CONFIRM_TEXT_NL  = "GELIEVE ZORGVULDIG DEZE OVEREENKOMST VAN DE VERGUNNING VAN SOFTWARE (\"LICENSE \") TE LEZEN ALVORENS DE SOFTWARE TE GEBRUIKEN.";

    private String              nodeName;

    private String              protocol;

    private String              sslProtocol;

    private String              hostname;

    private int                 hostport;

    private int                 hostportssl;

    private String              demoAppWebappName;

    private String              demoTicketWebappName;

    private String              demoPaymentWebappName;

    private String              demoLawyerWebappName;

    private String              demoPrescriptionWebappName;

    private String              demoMandateWebappName;

    private String              demoCinemaWebappName;

    private String              demoBankWebappName;


    public DemoStartableBean() {

        ResourceBundle properties = ResourceBundle.getBundle("config");
        this.nodeName = properties.getString("olas.node.name");
        this.sslProtocol = properties.getString("olas.host.protocol.ssl");
        this.protocol = properties.getString("olas.host.protocol");
        this.hostname = properties.getString("olas.host.name");
        this.hostport = Integer.parseInt(properties.getString("olas.host.port"));
        this.hostportssl = Integer.parseInt(properties.getString("olas.host.port.ssl"));
        this.demoAppWebappName = properties.getString("olas.demo.app.webapp.name");
        this.demoTicketWebappName = properties.getString("olas.demo.ticket.webapp.name");
        this.demoPaymentWebappName = properties.getString("olas.demo.payment.webapp.name");
        this.demoLawyerWebappName = properties.getString("olas.demo.lawyer.webapp.name");
        this.demoPrescriptionWebappName = properties.getString("olas.demo.prescription.webapp.name");
        this.demoMandateWebappName = properties.getString("olas.demo.mandate.webapp.name");
        this.demoCinemaWebappName = properties.getString("olas.demo.cinema.webapp.name");
        this.demoBankWebappName = properties.getString("olas.demo.bank.webapp.name");

        configureNode();

        PrivateKeyEntry demoPrivateKeyEntry = DemoKeyStoreUtil.getPrivateKeyEntry();
        X509Certificate demoCertificate = (X509Certificate) demoPrivateKeyEntry.getCertificate();
        try {
            this.registeredApplications.add(new Application(DEMO_APPLICATION_NAME, "owner", null, new URL(
                    this.protocol, this.hostname, this.hostport, "/" + this.demoAppWebappName), getLogo(), null, true,
                    true, demoCertificate, false, IdScopeType.USER, true, new URL(this.sslProtocol, this.hostname,
                            this.hostportssl, "/" + this.demoAppWebappName + "/logout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        configDemoUsers();

        this.trustedCertificates.put(demoCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);

        configTicketDemo();

        configBankDemo();

        configCinemaDemo();

        configPaymentDemo();

        configLawyerDemo();

        configPrescriptionDemo();

        configMandateDemo();
    }

    private byte[] getLogo() {

        return getLogo("/logo.jpg");
    }

    private void configMandateDemo() {

        PrivateKeyEntry demoMandatePrivateKeyEntry = DemoMandateKeyStoreUtils.getPrivateKeyEntry();
        X509Certificate demoMandateCertificate = (X509Certificate) demoMandatePrivateKeyEntry.getCertificate();

        /*
         * Register the application and the application certificate.
         */
        this.trustedCertificates.put(demoMandateCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        try {
            this.registeredApplications.add(new Application(DEMO_MANDATE_APPLICATION_NAME, "owner", null, new URL(
                    this.protocol, this.hostname, this.hostport, "/" + this.demoMandateWebappName), getLogo(), null,
                    true, true, demoMandateCertificate, true, IdScopeType.APPLICATION, true, new URL(this.sslProtocol,
                            this.hostname, this.hostportssl, "/" + this.demoMandateWebappName + "/authlogout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        /*
         * Subscribe the demo users to the mandate demo application.
         */
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli",
                DEMO_MANDATE_APPLICATION_NAME));
        this.subscriptions
                .add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", DEMO_MANDATE_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", DEMO_MANDATE_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute",
                DEMO_MANDATE_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo",
                DEMO_MANDATE_APPLICATION_NAME));

        /*
         * Register mandate attribute type
         */
        AttributeTypeEntity mandateCompanyAttributeType = configDemoAttribute(
                DemoConstants.MANDATE_COMPANY_ATTRIBUTE_NAME, DatatypeType.STRING, true, null, "Company", "Bedrijf",
                true, false);
        AttributeTypeEntity mandateTitleAttributeType = configDemoAttribute(DemoConstants.MANDATE_TITLE_ATTRIBUTE_NAME,
                DatatypeType.STRING, true, null, "Title", "Titel", true, false);

        AttributeTypeEntity mandateAttributeType = new AttributeTypeEntity(DemoConstants.MANDATE_ATTRIBUTE_NAME,
                DatatypeType.COMPOUNDED, true, false);
        mandateAttributeType.setMultivalued(true);
        mandateAttributeType.addMember(mandateCompanyAttributeType, 0, true);
        mandateAttributeType.addMember(mandateTitleAttributeType, 1, true);
        this.attributeTypes.add(mandateAttributeType);

        AttributeProviderEntity attributeProvider = new AttributeProviderEntity();
        attributeProvider.setPk(new AttributeProviderPK(DEMO_MANDATE_APPLICATION_NAME,
                DemoConstants.MANDATE_ATTRIBUTE_NAME));
        this.attributeProviders.add(attributeProvider);

        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(mandateAttributeType, Locale.ENGLISH
                .getLanguage(), "Mandate", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(mandateAttributeType, "nl", "Mandaat",
                null));

        /*
         * Application Identities
         */
        this.identities.add(new Identity(DEMO_MANDATE_APPLICATION_NAME,
                new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME,
                        true, false) }));

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(DEMO_MANDATE_APPLICATION_NAME);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n"
                + "Lin-k NV" + "\n" + "Software License Agreement for " + DEMO_MANDATE_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + DEMO_MANDATE_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        this.usageAgreements.add(usageAgreement);

        /*
         * Register admin
         */
        String mandateAdmin = "mandate-admin";
        this.authorizedUsers.put(mandateAdmin, new AuthenticationDevice("secret", null, null));

        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, mandateAdmin,
                DEMO_MANDATE_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, mandateAdmin,
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        /*
         * WS-Notification subscriptions
         */
        configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, demoMandateCertificate);

    }

    private void configTicketDemo() {

        PrivateKeyEntry demoTicketPrivateKeyEntry = DemoTicketKeyStoreUtils.getPrivateKeyEntry();
        X509Certificate demoTicketCertificate = (X509Certificate) demoTicketPrivateKeyEntry.getCertificate();

        this.trustedCertificates.put(demoTicketCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        try {
            this.registeredApplications.add(new Application(DEMO_TICKET_APPLICATION_NAME, "owner", null, new URL(
                    this.protocol, this.hostname, this.hostport, "/" + this.demoTicketWebappName),
                    getLogo("/eticket-small.png"), null, true, true, demoTicketCertificate, false,
                    IdScopeType.SUBSCRIPTION, true, new URL(this.sslProtocol, this.hostname, this.hostportssl, "/"
                            + this.demoTicketWebappName + "/logout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        this.identities.add(new Identity(DEMO_TICKET_APPLICATION_NAME, new IdentityAttributeTypeDO[] {
                new IdentityAttributeTypeDO(BeIdConstants.NRN_ATTRIBUTE, true, false),
                new IdentityAttributeTypeDO(DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        List<String> tempAllowedDevices = new LinkedList<String>();
        tempAllowedDevices.add(BeIdConstants.BEID_DEVICE_ID);
        this.allowedDevices.put(DEMO_TICKET_APPLICATION_NAME, tempAllowedDevices);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(DEMO_TICKET_APPLICATION_NAME);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n"
                + "Lin-k NV" + "\n" + "Software License Agreement for " + DEMO_TICKET_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + DEMO_TICKET_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        this.usageAgreements.add(usageAgreement);

        /*
         * WS-Notification subscriptions
         */
        configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, demoTicketCertificate);

    }

    private void configBankDemo() {

        PrivateKeyEntry demoBankPrivateKeyEntry = DemoBankKeyStoreUtils.getPrivateKeyEntry();
        X509Certificate demoBankCertificate = (X509Certificate) demoBankPrivateKeyEntry.getCertificate();

        this.trustedCertificates.put(demoBankCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        try {
            this.registeredApplications.add(new Application(DEMO_BANK_APPLICATION_NAME, "owner", null, new URL(
                    this.protocol, this.hostname, this.hostport, "/" + this.demoBankWebappName),
                    getLogo("/ebank-small.png"), null, true, true, demoBankCertificate, false,
                    IdScopeType.SUBSCRIPTION, true, new URL(this.sslProtocol, this.hostname, this.hostportssl, "/"
                            + this.demoBankWebappName + "/logout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        this.identities.add(new Identity(DEMO_BANK_APPLICATION_NAME,
                new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME,
                        true, false) }));

        List<String> tempAllowedDevices = new LinkedList<String>();
        tempAllowedDevices.add(BeIdConstants.BEID_DEVICE_ID);
        tempAllowedDevices.add(EncapConstants.ENCAP_DEVICE_ID);
        this.allowedDevices.put(DEMO_BANK_APPLICATION_NAME, tempAllowedDevices);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(DEMO_BANK_APPLICATION_NAME);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n"
                + "Lin-k NV" + "\n" + "Software License Agreement for " + DEMO_BANK_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + DEMO_BANK_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        this.usageAgreements.add(usageAgreement);

        /*
         * WS-Notification subscriptions
         */
        configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, demoBankCertificate);
    }

    private void configCinemaDemo() {

        PrivateKeyEntry demoCinemaPrivateKeyEntry = DemoCinemaKeyStoreUtils.getPrivateKeyEntry();
        X509Certificate demoCinemaCertificate = (X509Certificate) demoCinemaPrivateKeyEntry.getCertificate();

        this.trustedCertificates.put(demoCinemaCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        try {
            this.registeredApplications.add(new Application(DEMO_CINEMA_APPLICATION_NAME, "owner", null, new URL(
                    this.protocol, this.hostname, this.hostport, "/" + this.demoCinemaWebappName),
                    getLogo("/cinema.png"), null, true, true, demoCinemaCertificate, false, IdScopeType.SUBSCRIPTION,
                    true, new URL(this.sslProtocol, this.hostname, this.hostportssl, "/" + this.demoCinemaWebappName
                            + "/logout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        this.identities.add(new Identity(DEMO_CINEMA_APPLICATION_NAME, new IdentityAttributeTypeDO[] {
                new IdentityAttributeTypeDO(BeIdConstants.NRN_ATTRIBUTE, false, false),
                new IdentityAttributeTypeDO(DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        // Uncomment this to restrict cinema access through BeID device.
        List<String> tempAllowedDevices = new LinkedList<String>();
        tempAllowedDevices.add(BeIdConstants.BEID_DEVICE_ID);
        tempAllowedDevices.add(EncapConstants.ENCAP_DEVICE_ID);
        tempAllowedDevices.add(OptionConstants.OPTION_DEVICE_ID);
        this.allowedDevices.put(DEMO_CINEMA_APPLICATION_NAME, tempAllowedDevices);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(DEMO_CINEMA_APPLICATION_NAME);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n"
                + "Lin-k NV" + "\n" + "Software License Agreement for " + DEMO_CINEMA_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + DEMO_CINEMA_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        this.usageAgreements.add(usageAgreement);

        /*
         * WS-Notification subscriptions
         */
        configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, demoCinemaCertificate);

    }

    private void configPaymentDemo() {

        String paymentAdmin = "payment-admin";
        this.authorizedUsers.put(paymentAdmin, new AuthenticationDevice("secret", null, null));

        /*
         * Register the payment and ticket demo application within SafeOnline.
         */
        PrivateKeyEntry demoPaymentPrivateKeyEntry = DemoPaymentKeyStoreUtils.getPrivateKeyEntry();
        X509Certificate demoPaymentCertificate = (X509Certificate) demoPaymentPrivateKeyEntry.getCertificate();

        try {
            this.registeredApplications.add(new Application(DEMO_PAYMENT_APPLICATION_NAME, "owner", null, new URL(
                    this.protocol, this.hostname, this.hostport, "/" + this.demoPaymentWebappName),
                    getLogo("/ebank-small.png"), null, true, true, demoPaymentCertificate, true,
                    IdScopeType.SUBSCRIPTION, true, new URL(this.sslProtocol, this.hostname, this.hostportssl, "/"
                            + this.demoPaymentWebappName + "/authlogout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        this.trustedCertificates.put(demoPaymentCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);

        /*
         * Subscribe the payment admin.
         */
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, paymentAdmin,
                DEMO_PAYMENT_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, paymentAdmin,
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        /*
         * Subscribe the demo users to the payment demo application.
         */
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli",
                DEMO_PAYMENT_APPLICATION_NAME));
        this.subscriptions
                .add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", DEMO_PAYMENT_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", DEMO_PAYMENT_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute",
                DEMO_PAYMENT_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo",
                DEMO_PAYMENT_APPLICATION_NAME));

        /*
         * Attribute Types.
         */
        configDemoAttribute(DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, false,
                DEMO_PAYMENT_APPLICATION_NAME, "Junior Account", "Jongerenrekening", true, false);
        configDemoAttribute(DemoConstants.DEMO_VISA_ATTRIBUTE_NAME, DatatypeType.STRING, true, null, "VISA number",
                "VISA nummer", true, true);

        /*
         * Application Identities.
         */
        this.identities.add(new Identity(DEMO_PAYMENT_APPLICATION_NAME, new IdentityAttributeTypeDO[] {
                new IdentityAttributeTypeDO(DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.DEMO_VISA_ATTRIBUTE_NAME, true, false),
                new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        /*
         * device restriction
         */
        List<String> tempAllowedDevices = new LinkedList<String>();
        tempAllowedDevices.add(BeIdConstants.BEID_DEVICE_ID);
        tempAllowedDevices.add(DigipassConstants.DIGIPASS_DEVICE_ID);
        tempAllowedDevices.add(EncapConstants.ENCAP_DEVICE_ID);
        tempAllowedDevices.add(OptionConstants.OPTION_DEVICE_ID);
        this.allowedDevices.put(DEMO_PAYMENT_APPLICATION_NAME, tempAllowedDevices);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(DEMO_PAYMENT_APPLICATION_NAME);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n"
                + "Lin-k NV" + "\n" + "Software License Agreement for " + DEMO_PAYMENT_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + DEMO_PAYMENT_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        this.usageAgreements.add(usageAgreement);

        /*
         * WS-Notification subscriptions
         */
        configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, demoPaymentCertificate);

    }

    private void configPrescriptionDemo() {

        /*
         * Register the prescription demo application within SafeOnline.
         */
        PrivateKeyEntry demoPrescriptionPrivateKeyEntry = DemoPrescriptionKeyStoreUtils.getPrivateKeyEntry();
        X509Certificate demoPrescriptionCertificate = (X509Certificate) demoPrescriptionPrivateKeyEntry
                .getCertificate();
        this.trustedCertificates.put(demoPrescriptionCertificate,
                SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        try {
            this.registeredApplications.add(new Application(DEMO_PRESCRIPTION_APPLICATION_NAME, "owner", null, new URL(
                    this.protocol, this.hostname, this.hostport, "/" + this.demoPrescriptionWebappName), getLogo(),
                    null, true, true, demoPrescriptionCertificate, true, IdScopeType.SUBSCRIPTION, true, new URL(
                            this.sslProtocol, this.hostname, this.hostportssl, "/" + this.demoPrescriptionWebappName
                                    + "/authlogout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        /*
         * Subscribe the prescription admin.
         */
        String prescriptionAdmin = "prescription-admin";
        this.authorizedUsers.put(prescriptionAdmin, new AuthenticationDevice("secret", null, null));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, prescriptionAdmin,
                DEMO_PRESCRIPTION_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, prescriptionAdmin,
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        /*
         * Subscribe the demo users to the prescription demo application.
         */
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli",
                DEMO_PRESCRIPTION_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter",
                DEMO_PRESCRIPTION_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario",
                DEMO_PRESCRIPTION_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute",
                DEMO_PRESCRIPTION_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo",
                DEMO_PRESCRIPTION_APPLICATION_NAME));

        /*
         * Attribute Types.
         */
        configDemoAttribute(DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, false,
                DEMO_PRESCRIPTION_APPLICATION_NAME, "Prescription Admin", "Voorschriftbeheerder", true, false);
        configDemoAttribute(DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, false,
                DEMO_PRESCRIPTION_APPLICATION_NAME, "Care Provider", "Dokter", true, false);
        configDemoAttribute(DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, false,
                DEMO_PRESCRIPTION_APPLICATION_NAME, "Pharmacist", "Apotheker", true, false);

        /*
         * Application Identities.
         */
        this.identities.add(new Identity(DEMO_PRESCRIPTION_APPLICATION_NAME, new IdentityAttributeTypeDO[] {
                new IdentityAttributeTypeDO(DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        /*
         * Also make sure the admin is marked as such.
         */
        AttributeEntity prescriptionAdminAttribute = new AttributeEntity();
        prescriptionAdminAttribute.setPk(new AttributePK(DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME,
                prescriptionAdmin));
        prescriptionAdminAttribute.setBooleanValue(true);
        this.attributes.add(prescriptionAdminAttribute);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(DEMO_PRESCRIPTION_APPLICATION_NAME);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n"
                + "Lin-k NV" + "\n" + "Software License Agreement for " + DEMO_PRESCRIPTION_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + DEMO_PRESCRIPTION_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        this.usageAgreements.add(usageAgreement);
    }

    private void configLawyerDemo() {

        PrivateKeyEntry demoLawyerPrivateKeyEntry = DemoLawyerKeyStoreUtils.getPrivateKeyEntry();
        X509Certificate demoLawyerCertificate = (X509Certificate) demoLawyerPrivateKeyEntry.getCertificate();
        try {
            this.registeredApplications.add(new Application(DEMO_LAWYER_APPLICATION_NAME, "owner", null, new URL(
                    this.protocol, this.hostname, this.hostport, "/" + this.demoLawyerWebappName), getLogo(), null,
                    true, true, demoLawyerCertificate, true, IdScopeType.SUBSCRIPTION, true, new URL(this.sslProtocol,
                            this.hostname, this.hostportssl, "/" + this.demoLawyerWebappName + "/authlogout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }
        this.trustedCertificates.put(demoLawyerCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);

        this.authorizedUsers.put("baradmin", new AuthenticationDevice("secret", null, null));

        this.subscriptions
                .add(new Subscription(SubscriptionOwnerType.SUBJECT, "baradmin", DEMO_LAWYER_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "baradmin",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        this.subscriptions
                .add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli", DEMO_LAWYER_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", DEMO_LAWYER_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", DEMO_LAWYER_APPLICATION_NAME));
        this.subscriptions
                .add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute", DEMO_LAWYER_APPLICATION_NAME));
        this.subscriptions
                .add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo", DEMO_LAWYER_APPLICATION_NAME));

        configLawyerDemoAttribute(DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME, DatatypeType.BOOLEAN,
                "Bar administrator", "Baliebeheerder");
        configLawyerDemoAttribute(DemoConstants.LAWYER_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, "Lawyer", "Advocaat");
        configLawyerDemoAttribute(DemoConstants.LAWYER_BAR_ATTRIBUTE_NAME, DatatypeType.STRING, "Bar", "Balie");
        configLawyerDemoAttribute(DemoConstants.LAWYER_SUSPENDED_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, "Suspended",
                "Geschorst");

        this.identities.add(new Identity(DEMO_LAWYER_APPLICATION_NAME, new IdentityAttributeTypeDO[] {
                new IdentityAttributeTypeDO(DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.LAWYER_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.LAWYER_BAR_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.LAWYER_SUSPENDED_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        /*
         * Also make sure the baradmin is marked as such.
         */
        AttributeEntity barAdminBarAdminAttribute = new AttributeEntity();
        barAdminBarAdminAttribute.setPk(new AttributePK(DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME, "baradmin"));
        barAdminBarAdminAttribute.setBooleanValue(true);
        this.attributes.add(barAdminBarAdminAttribute);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(DEMO_LAWYER_APPLICATION_NAME);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n"
                + "Lin-k NV" + "\n" + "Software License Agreement for " + DEMO_LAWYER_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + DEMO_LAWYER_APPLICATION_NAME + "\n\n"
                + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        this.usageAgreements.add(usageAgreement);
    }

    private void configLawyerDemoAttribute(String attributeName, DatatypeType datatype, String enName, String nlName) {

        configDemoAttribute(attributeName, datatype, false, DEMO_LAWYER_APPLICATION_NAME, enName, nlName, true, false);
    }

    private AttributeTypeEntity configDemoAttribute(String attributeName, DatatypeType datatype, boolean multiValued,
            String attributeProviderName, String enName, String nlName, boolean userVisible, boolean userEditable) {

        AttributeTypeEntity attributeType = new AttributeTypeEntity(attributeName, datatype, userVisible, userEditable);
        attributeType.setMultivalued(multiValued);
        this.attributeTypes.add(attributeType);

        if (null != attributeProviderName) {
            AttributeProviderEntity attributeProvider = new AttributeProviderEntity();
            attributeProvider.setPk(new AttributeProviderPK(attributeProviderName, attributeName));
            this.attributeProviders.add(attributeProvider);
        }

        if (null != enName) {
            this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(attributeType, Locale.ENGLISH
                    .getLanguage(), enName, null));
        }
        if (null != nlName) {
            this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(attributeType, "nl", nlName, null));
        }
        return attributeType;
    }

    private void configDemoUsers() {

        this.authorizedUsers.put("fcorneli", new AuthenticationDevice("secret", null, null));
        this.authorizedUsers.put("dieter", new AuthenticationDevice("secret", null, null));
        this.authorizedUsers.put("mario", new AuthenticationDevice("secret", null, null));
        this.authorizedUsers.put("wvdhaute", new AuthenticationDevice("secret", new String[] { "95874644" }, null));
        this.authorizedUsers.put("mbillemo", new AuthenticationDevice("secret", null, null));

        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli", DEMO_APPLICATION_NAME));
        this.subscriptions
                .add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli", DEMO_TICKET_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli",
                DEMO_PAYMENT_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "fcorneli",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", DEMO_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", DEMO_TICKET_APPLICATION_NAME));
        this.subscriptions
                .add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", DEMO_PAYMENT_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "dieter",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", DEMO_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", DEMO_TICKET_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", DEMO_PAYMENT_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "mario",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute", DEMO_APPLICATION_NAME));
        this.subscriptions
                .add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute", DEMO_TICKET_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute",
                DEMO_PAYMENT_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "wvdhaute",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo", DEMO_APPLICATION_NAME));
        this.subscriptions
                .add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo", DEMO_TICKET_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo",
                DEMO_PAYMENT_APPLICATION_NAME));
        this.subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "mbillemo",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

    }

    private void configSubscription(String topic, X509Certificate certificate) {

        String address = this.protocol + "://" + this.hostname + ":";
        if (this.protocol.equals("http")) {
            address += this.hostport;
        } else {
            address += this.hostportssl;
        }
        address += "/safe-online-demo-ws/consumer";
        this.notificationSubcriptions.add(new NotificationSubscription(topic, address, certificate));
    }

    private void configureNode() {

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        IdentityServiceClient identityServiceClient = new IdentityServiceClient();

        this.node = new Node(this.nodeName, this.sslProtocol, this.hostname, this.hostport, this.hostportssl,
                authIdentityServiceClient.getCertificate(), identityServiceClient.getCertificate());
        this.trustedCertificates.put(authIdentityServiceClient.getCertificate(),
                SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
    }

    @Override
    public int getPriority() {

        return BeIdConstants.BEID_BOOT_PRIORITY - 1;
    }
}
