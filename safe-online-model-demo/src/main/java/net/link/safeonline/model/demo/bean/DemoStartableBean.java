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
import net.link.safeonline.demo.keystore.DemoKeyStore;
import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStore;
import net.link.safeonline.demo.mandate.keystore.DemoMandateKeyStore;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStore;
import net.link.safeonline.demo.prescription.keystore.DemoPrescriptionKeyStore;
import net.link.safeonline.demo.ticket.keystore.DemoTicketKeyStore;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributePK;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.keystore.SafeOnlineKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.model.digipass.DigipassConstants;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.model.password.PasswordManager;
import net.link.safeonline.service.SubjectService;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = DemoStartableBean.JNDI_BINDING)
public class DemoStartableBean extends AbstractInitBean {

    public static final String  JNDI_BINDING                      = DemoConstants.DEMO_STARTABLE_JNDI_PREFIX + "DemoStartableBean";

    private static final String PASSWORD                          = "secret";

    public static final String  LICENSE_AGREEMENT_CONFIRM_TEXT_EN = "PLEASE READ THIS SOFTWARE LICENSE AGREEMENT (\"LICENSE\") CAREFULLY BEFORE USING THE SOFTWARE. \n BY USING THE SOFTWARE, YOU ARE AGREEING TO BE BOUND BY THE TERMS OF THIS LICENSE. \n IF YOU ARE ACCESSING THE SOFTWARE ELECTRONICALLY, SIGNIFY YOUR AGREEMENT TO BE BOUND BY THE TERMS OF THIS LICENSE BY CLICKING THE \"AGREE/ACCEPT\" BUTTON. \n IF YOU DO NOT AGREE TO THE TERMS OF THIS LICENSE, DO NOT USE THE SOFTWARE AND (IF APPLICABLE) RETURN THE APPLE SOFTWARE TO THE PLACE WHERE YOU OBTAINED IT FOR A REFUND OR, IF THE SOFTWARE WAS ACCESSED ELECTRONICALLY, CLICK \"DISAGREE/DECLINE\".";

    public static final String  LICENSE_AGREEMENT_CONFIRM_TEXT_NL = "GELIEVE ZORGVULDIG DEZE OVEREENKOMST VAN DE VERGUNNING VAN SOFTWARE (\"LICENSE \") TE LEZEN ALVORENS DE SOFTWARE TE GEBRUIKEN.";


    private static class PasswordRegistration {

        final String login;

        final String password;


        public PasswordRegistration(String login, String password) {

            this.login = login;
            this.password = password;
        }
    }


    private List<PasswordRegistration> passwordRegistrations;

    private String                     nodeName;

    private String                     protocol;

    private String                     sslProtocol;

    private String                     hostname;

    private int                        hostport;

    private int                        hostportssl;

    private String                     demoAppWebappName;
    private String                     demoTicketWebappName;
    private String                     demoPaymentWebappName;
    private String                     demoLawyerWebappName;
    private String                     demoPrescriptionWebappName;
    private String                     demoMandateWebappName;
    private String                     demoCinemaWebappName;
    private String                     demoBankWebappName;

    private String                     demoAppWebappUrl;
    private String                     demoTicketWebappUrl;
    private String                     demoPaymentWebappUrl;
    private String                     demoLawyerWebappUrl;
    private String                     demoPrescriptionWebappUrl;
    private String                     demoMandateWebappUrl;
    private String                     demoCinemaWebappUrl;
    private String                     demoBankWebappUrl;

    @EJB(mappedName = PasswordManager.JNDI_BINDING)
    private PasswordManager            passwordManager;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService             subjectService;


    public DemoStartableBean() {

        ResourceBundle properties = ResourceBundle.getBundle("config");
        nodeName = properties.getString("olas.node.name");
        sslProtocol = properties.getString("olas.host.protocol.ssl");
        protocol = properties.getString("olas.host.protocol");
        hostname = properties.getString("olas.host.name");
        hostport = Integer.parseInt(properties.getString("olas.host.port"));
        hostportssl = Integer.parseInt(properties.getString("olas.host.port.ssl"));
        demoAppWebappName = properties.getString("olas.demo.app.webapp.name");
        demoTicketWebappName = properties.getString("olas.demo.ticket.webapp.name");
        demoPaymentWebappName = properties.getString("olas.demo.payment.webapp.name");
        demoLawyerWebappName = properties.getString("olas.demo.lawyer.webapp.name");
        demoPrescriptionWebappName = properties.getString("olas.demo.prescription.webapp.name");
        demoMandateWebappName = properties.getString("olas.demo.mandate.webapp.name");
        demoCinemaWebappName = properties.getString("olas.demo.cinema.webapp.name");
        demoBankWebappName = properties.getString("olas.demo.bank.webapp.name");
        demoAppWebappUrl = properties.getString("olas.demo.app.webapp.url");
        demoTicketWebappUrl = properties.getString("olas.demo.ticket.webapp.url");
        demoPaymentWebappUrl = properties.getString("olas.demo.payment.webapp.url");
        demoLawyerWebappUrl = properties.getString("olas.demo.lawyer.webapp.url");
        demoPrescriptionWebappUrl = properties.getString("olas.demo.prescription.webapp.url");
        demoMandateWebappUrl = properties.getString("olas.demo.mandate.webapp.url");
        demoCinemaWebappUrl = properties.getString("olas.demo.cinema.webapp.url");
        demoBankWebappUrl = properties.getString("olas.demo.bank.webapp.url");
        passwordRegistrations = new LinkedList<PasswordRegistration>();

        configureNode();

        PrivateKeyEntry demoPrivateKeyEntry = DemoKeyStore.getPrivateKeyEntry();
        X509Certificate demoCertificate = (X509Certificate) demoPrivateKeyEntry.getCertificate();
        try {
            registeredApplications.add(new Application(demoAppWebappName, "owner", null, new URL(protocol, hostname, hostport, "/"
                    + demoAppWebappUrl), getLogo(), true, true, demoCertificate, false, IdScopeType.USER, true, new URL(sslProtocol,
                    hostname, hostportssl, "/" + demoAppWebappUrl + "/logout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        configDemoUsers();

        trustedCertificates.put(demoCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);

        configTicketDemo();

        configBankDemo();

        configCinemaDemo();

        configPaymentDemo();

        configLawyerDemo();

        configPrescriptionDemo();

        configMandateDemo();

    }

    @Override
    public void postStart() {

        super.postStart();

        for (PasswordRegistration passwordRegistration : passwordRegistrations) {

            SubjectEntity subject = subjectService.findSubjectFromUserName(passwordRegistration.login);
            if (null == subject) {
                try {
                    subject = subjectService.addSubject(passwordRegistration.login);
                } catch (AttributeTypeNotFoundException e) {
                    LOG.fatal("safeonline exception", e);
                    throw new EJBException(e);
                }
            }

            if (!passwordManager.isPasswordConfigured(subject)) {
                passwordManager.registerPassword(subject, passwordRegistration.password);
            }
        }

    }

    private byte[] getLogo() {

        return getLogo("/logo.jpg");
    }

    private void configMandateDemo() {

        PrivateKeyEntry demoMandatePrivateKeyEntry = DemoMandateKeyStore.getPrivateKeyEntry();
        X509Certificate demoMandateCertificate = (X509Certificate) demoMandatePrivateKeyEntry.getCertificate();

        /*
         * Register the application and the application certificate.
         */
        trustedCertificates.put(demoMandateCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        try {
            registeredApplications.add(new Application(demoMandateWebappName, "owner", null, new URL(protocol, hostname, hostport, "/"
                    + demoMandateWebappUrl), getLogo(), true, true, demoMandateCertificate, true, IdScopeType.APPLICATION, true, new URL(
                    sslProtocol, hostname, hostportssl, "/" + demoMandateWebappUrl + "/authlogout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        /*
         * Subscribe the demo users to the mandate demo application.
         */
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli", demoMandateWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", demoMandateWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", demoMandateWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute", demoMandateWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo", demoMandateWebappName));

        /*
         * Register mandate attribute type
         */
        AttributeTypeEntity mandateCompanyAttributeType = configDemoAttribute(DemoConstants.MANDATE_COMPANY_ATTRIBUTE_NAME,
                DatatypeType.STRING, true, null, "Company", "Bedrijf", true, false);
        AttributeTypeEntity mandateTitleAttributeType = configDemoAttribute(DemoConstants.MANDATE_TITLE_ATTRIBUTE_NAME,
                DatatypeType.STRING, true, null, "Title", "Titel", true, false);

        AttributeTypeEntity mandateAttributeType = new AttributeTypeEntity(DemoConstants.MANDATE_ATTRIBUTE_NAME, DatatypeType.COMPOUNDED,
                true, false);
        mandateAttributeType.setMultivalued(true);
        mandateAttributeType.addMember(mandateCompanyAttributeType, 0, true);
        mandateAttributeType.addMember(mandateTitleAttributeType, 1, true);
        attributeTypes.add(mandateAttributeType);

        attributeProviders.add(new AttributeProvider(demoMandateWebappName, DemoConstants.MANDATE_ATTRIBUTE_NAME));

        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(mandateAttributeType, Locale.ENGLISH.getLanguage(), "Mandate",
                null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(mandateAttributeType, "nl", "Mandaat", null));

        /*
         * Application Identities
         */
        identities.add(new Identity(demoMandateWebappName, new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
                DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(demoMandateWebappName);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n" + "Lin-k NV" + "\n"
                + "Software License Agreement for " + demoMandateWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + demoMandateWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        usageAgreements.add(usageAgreement);

        /*
         * Register admin
         */
        String mandateAdmin = "mandate-admin";
        users.add(mandateAdmin);
        passwordRegistrations.add(new PasswordRegistration(mandateAdmin, PASSWORD));

        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, mandateAdmin, demoMandateWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, mandateAdmin,
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        /*
         * WS-Notification subscriptions
         */
        configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, demoMandateCertificate);

    }

    private void configTicketDemo() {

        PrivateKeyEntry demoTicketPrivateKeyEntry = DemoTicketKeyStore.getPrivateKeyEntry();
        X509Certificate demoTicketCertificate = (X509Certificate) demoTicketPrivateKeyEntry.getCertificate();

        trustedCertificates.put(demoTicketCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        try {
            registeredApplications.add(new Application(demoTicketWebappName, "owner", null, new URL(protocol, hostname, hostport, "/"
                    + demoTicketWebappUrl), getLogo("/eticket-small.png"), true, true, demoTicketCertificate, false,
                    IdScopeType.SUBSCRIPTION, true, new URL(sslProtocol, hostname, hostportssl, "/" + demoTicketWebappUrl + "/logout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        identities.add(new Identity(demoTicketWebappName, new IdentityAttributeTypeDO[] {
                new IdentityAttributeTypeDO(BeIdConstants.BEID_NRN_ATTRIBUTE, true, false),
                new IdentityAttributeTypeDO(DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        List<String> tempAllowedDevices = new LinkedList<String>();
        tempAllowedDevices.add(BeIdConstants.BEID_DEVICE_ID);
        allowedDevices.put(demoTicketWebappName, tempAllowedDevices);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(demoTicketWebappName);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n" + "Lin-k NV" + "\n"
                + "Software License Agreement for " + demoTicketWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + demoTicketWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        usageAgreements.add(usageAgreement);

        /*
         * WS-Notification subscriptions
         */
        configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, demoTicketCertificate);
    }

    private void configBankDemo() {

        PrivateKeyEntry demoBankPrivateKeyEntry = DemoBankKeyStore.getPrivateKeyEntry();
        X509Certificate demoBankCertificate = (X509Certificate) demoBankPrivateKeyEntry.getCertificate();

        trustedCertificates.put(demoBankCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        try {
            registeredApplications.add(new Application(demoBankWebappName, "owner", null, new URL(protocol, hostname, hostport, "/"
                    + demoBankWebappUrl), getLogo("/ebank-small.png"), true, true, demoBankCertificate, false, IdScopeType.SUBSCRIPTION,
                    true, new URL(sslProtocol, hostname, hostportssl, "/" + demoBankWebappUrl + "/logout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        identities.add(new Identity(demoBankWebappName, new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
                DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        List<String> tempAllowedDevices = new LinkedList<String>();
        tempAllowedDevices.add(BeIdConstants.BEID_DEVICE_ID);
        tempAllowedDevices.add(EncapConstants.ENCAP_DEVICE_ID);
        allowedDevices.put(demoBankWebappName, tempAllowedDevices);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(demoBankWebappName);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n" + "Lin-k NV" + "\n"
                + "Software License Agreement for " + demoBankWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + demoBankWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        usageAgreements.add(usageAgreement);

        /*
         * WS-Notification subscriptions
         */
        configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, demoBankCertificate);
        configSubscription(SafeOnlineConstants.TOPIC_UNSUBSCRIBE_USER, demoBankCertificate);
    }

    private void configCinemaDemo() {

        PrivateKeyEntry demoCinemaPrivateKeyEntry = DemoCinemaKeyStore.getPrivateKeyEntry();
        X509Certificate demoCinemaCertificate = (X509Certificate) demoCinemaPrivateKeyEntry.getCertificate();

        trustedCertificates.put(demoCinemaCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        try {
            registeredApplications.add(new Application(demoCinemaWebappName, "owner", null, new URL(protocol, hostname, hostport, "/"
                    + demoCinemaWebappUrl), getLogo("/ecinema-small.png"), true, true, demoCinemaCertificate, false,
                    IdScopeType.SUBSCRIPTION, true, new URL(sslProtocol, hostname, hostportssl, "/" + demoCinemaWebappUrl + "/logout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        identities.add(new Identity(demoCinemaWebappName, new IdentityAttributeTypeDO[] {
                new IdentityAttributeTypeDO(BeIdConstants.BEID_NRN_ATTRIBUTE, false, false),
                new IdentityAttributeTypeDO(DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        // Uncomment this to restrict cinema access through BeID device.
        List<String> tempAllowedDevices = new LinkedList<String>();
        tempAllowedDevices.add(BeIdConstants.BEID_DEVICE_ID);
        tempAllowedDevices.add(EncapConstants.ENCAP_DEVICE_ID);
        allowedDevices.put(demoCinemaWebappName, tempAllowedDevices);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(demoCinemaWebappName);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n" + "Lin-k NV" + "\n"
                + "Software License Agreement for " + demoCinemaWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + demoCinemaWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        usageAgreements.add(usageAgreement);

        /*
         * WS-Notification subscriptions
         */
        configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, demoCinemaCertificate);
    }

    private void configPaymentDemo() {

        String paymentAdmin = "payment-admin";
        users.add(paymentAdmin);
        passwordRegistrations.add(new PasswordRegistration(paymentAdmin, PASSWORD));

        /*
         * Register the payment and ticket demo application within SafeOnline.
         */
        PrivateKeyEntry demoPaymentPrivateKeyEntry = DemoPaymentKeyStore.getPrivateKeyEntry();
        X509Certificate demoPaymentCertificate = (X509Certificate) demoPaymentPrivateKeyEntry.getCertificate();

        try {
            registeredApplications.add(new Application(demoPaymentWebappName, "owner", null, new URL(protocol, hostname, hostport, "/"
                    + demoPaymentWebappUrl), getLogo("/epayment-small.png"), true, true, demoPaymentCertificate, true,
                    IdScopeType.SUBSCRIPTION, true, new URL(sslProtocol, hostname, hostportssl, "/" + demoPaymentWebappUrl + "/logout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        trustedCertificates.put(demoPaymentCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);

        /*
         * Subscribe the payment admin.
         */
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, paymentAdmin, demoPaymentWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, paymentAdmin,
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        /*
         * Subscribe the demo users to the payment demo application.
         */
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli", demoPaymentWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", demoPaymentWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", demoPaymentWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute", demoPaymentWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo", demoPaymentWebappName));

        /*
         * Attribute Types.
         */
        configDemoAttribute(DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, false, demoPaymentWebappName,
                "Junior Account", "Jongerenrekening", true, false);
        configDemoAttribute(DemoConstants.DEMO_VISA_ATTRIBUTE_NAME, DatatypeType.STRING, true, null, "VISA number", "VISA nummer", true,
                true);

        /*
         * Application Identities.
         */
        identities.add(new Identity(demoPaymentWebappName, new IdentityAttributeTypeDO[] {
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
        allowedDevices.put(demoPaymentWebappName, tempAllowedDevices);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(demoPaymentWebappName);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n" + "Lin-k NV" + "\n"
                + "Software License Agreement for " + demoPaymentWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + demoPaymentWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        usageAgreements.add(usageAgreement);

        /*
         * WS-Notification subscriptions
         */
        configSubscription(SafeOnlineConstants.TOPIC_REMOVE_USER, demoPaymentCertificate);

    }

    private void configPrescriptionDemo() {

        /*
         * Register the prescription demo application within SafeOnline.
         */
        PrivateKeyEntry demoPrescriptionPrivateKeyEntry = DemoPrescriptionKeyStore.getPrivateKeyEntry();
        X509Certificate demoPrescriptionCertificate = (X509Certificate) demoPrescriptionPrivateKeyEntry.getCertificate();
        trustedCertificates.put(demoPrescriptionCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        try {
            registeredApplications.add(new Application(demoPrescriptionWebappName, "owner", null, new URL(protocol, hostname, hostport, "/"
                    + demoPrescriptionWebappUrl), getLogo(), true, true, demoPrescriptionCertificate, true, IdScopeType.SUBSCRIPTION, true,
                    new URL(sslProtocol, hostname, hostportssl, "/" + demoPrescriptionWebappUrl + "/authlogout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }

        /*
         * Subscribe the prescription admin.
         */
        String prescriptionAdmin = "prescription-admin";
        users.add(prescriptionAdmin);
        passwordRegistrations.add(new PasswordRegistration(prescriptionAdmin, PASSWORD));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, prescriptionAdmin, demoPrescriptionWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, prescriptionAdmin,
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        /*
         * Subscribe the demo users to the prescription demo application.
         */
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli", demoPrescriptionWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", demoPrescriptionWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", demoPrescriptionWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute", demoPrescriptionWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo", demoPrescriptionWebappName));

        /*
         * Attribute Types.
         */
        AttributeTypeEntity prescriptionAdminAttributeType = configDemoAttribute(DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME,
                DatatypeType.BOOLEAN, false, demoPrescriptionWebappName, "Prescription Admin", "Voorschriftbeheerder", true, false);
        configDemoAttribute(DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, false,
                demoPrescriptionWebappName, "Care Provider", "Dokter", true, false);
        configDemoAttribute(DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, false, demoPrescriptionWebappName,
                "Pharmacist", "Apotheker", true, false);

        /*
         * Application Identities.
         */
        identities.add(new Identity(demoPrescriptionWebappName, new IdentityAttributeTypeDO[] {
                new IdentityAttributeTypeDO(DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        /*
         * Also make sure the admin is marked as such.
         */
        AttributeEntity prescriptionAdminAttribute = new AttributeEntity();
        prescriptionAdminAttribute.setAttributeType(prescriptionAdminAttributeType);
        prescriptionAdminAttribute.setPk(new AttributePK(DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME, prescriptionAdmin));
        prescriptionAdminAttribute.setValue(true);
        attributes.add(prescriptionAdminAttribute);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(demoPrescriptionWebappName);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n" + "Lin-k NV" + "\n"
                + "Software License Agreement for " + demoPrescriptionWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + demoPrescriptionWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        usageAgreements.add(usageAgreement);
    }

    private void configLawyerDemo() {

        PrivateKeyEntry demoLawyerPrivateKeyEntry = DemoLawyerKeyStore.getPrivateKeyEntry();
        X509Certificate demoLawyerCertificate = (X509Certificate) demoLawyerPrivateKeyEntry.getCertificate();
        try {
            registeredApplications.add(new Application(demoLawyerWebappName, "owner", null, new URL(protocol, hostname, hostport, "/"
                    + demoLawyerWebappUrl), getLogo(), true, true, demoLawyerCertificate, true, IdScopeType.SUBSCRIPTION, true, new URL(
                    sslProtocol, hostname, hostportssl, "/" + demoLawyerWebappUrl + "/authlogout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed URL Exception: " + e.getMessage());
        }
        trustedCertificates.put(demoLawyerCertificate, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);

        String baradmin = "baradmin";
        users.add(baradmin);
        passwordRegistrations.add(new PasswordRegistration(baradmin, PASSWORD));

        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "baradmin", demoLawyerWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "baradmin",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli", demoLawyerWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", demoLawyerWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", demoLawyerWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute", demoLawyerWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo", demoLawyerWebappName));

        AttributeTypeEntity barAdminAttributeType = configLawyerDemoAttribute(DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME,
                DatatypeType.BOOLEAN, "Bar administrator", "Baliebeheerder");
        configLawyerDemoAttribute(DemoConstants.LAWYER_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, "Lawyer", "Advocaat");
        configLawyerDemoAttribute(DemoConstants.LAWYER_BAR_ATTRIBUTE_NAME, DatatypeType.STRING, "Bar", "Balie");
        configLawyerDemoAttribute(DemoConstants.LAWYER_SUSPENDED_ATTRIBUTE_NAME, DatatypeType.BOOLEAN, "Suspended", "Geschorst");

        identities.add(new Identity(demoLawyerWebappName, new IdentityAttributeTypeDO[] {
                new IdentityAttributeTypeDO(DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.LAWYER_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.LAWYER_BAR_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.LAWYER_SUSPENDED_ATTRIBUTE_NAME, false, false),
                new IdentityAttributeTypeDO(DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, true, false) }));

        /*
         * Also make sure the baradmin is marked as such.
         */
        AttributeEntity barAdminAttribute = new AttributeEntity();
        barAdminAttribute.setAttributeType(barAdminAttributeType);
        barAdminAttribute.setPk(new AttributePK(DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME, "baradmin"));
        barAdminAttribute.setValue(true);
        attributes.add(barAdminAttribute);

        /*
         * Application usage agreements
         */
        UsageAgreement usageAgreement = new UsageAgreement(demoLawyerWebappName);
        usageAgreement.addUsageAgreementText(new UsageAgreementText(Locale.ENGLISH.getLanguage(), "English" + "\n\n" + "Lin-k NV" + "\n"
                + "Software License Agreement for " + demoLawyerWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_EN));
        usageAgreement.addUsageAgreementText(new UsageAgreementText("nl", "Nederlands" + "\n\n" + "Lin-k NV" + "\n"
                + "Software Gebruikers Overeenkomst voor " + demoLawyerWebappName + "\n\n" + LICENSE_AGREEMENT_CONFIRM_TEXT_NL));
        usageAgreements.add(usageAgreement);
    }

    private AttributeTypeEntity configLawyerDemoAttribute(String attributeName, DatatypeType datatype, String enName, String nlName) {

        return configDemoAttribute(attributeName, datatype, false, demoLawyerWebappName, enName, nlName, true, false);
    }

    private AttributeTypeEntity configDemoAttribute(String attributeName, DatatypeType datatype, boolean multiValued,
                                                    String attributeProviderName, String enName, String nlName, boolean userVisible,
                                                    boolean userEditable) {

        AttributeTypeEntity attributeType = new AttributeTypeEntity(attributeName, datatype, userVisible, userEditable);
        attributeType.setMultivalued(multiValued);
        attributeTypes.add(attributeType);

        if (null != attributeProviderName) {
            attributeProviders.add(new AttributeProvider(attributeProviderName, attributeName));
        }

        if (null != enName) {
            attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(attributeType, Locale.ENGLISH.getLanguage(), enName, null));
        }
        if (null != nlName) {
            attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(attributeType, "nl", nlName, null));
        }
        return attributeType;
    }

    private void configDemoUsers() {

        // TODO: add password registration
        users.add("fcorneli");
        users.add("dieter");
        users.add("mario");
        users.add("wvdhaute");
        users.add("mbillemo");
        passwordRegistrations.add(new PasswordRegistration("fcorneli", PASSWORD));
        passwordRegistrations.add(new PasswordRegistration("dieter", PASSWORD));
        passwordRegistrations.add(new PasswordRegistration("mario", PASSWORD));
        passwordRegistrations.add(new PasswordRegistration("wvdhaute", PASSWORD));
        passwordRegistrations.add(new PasswordRegistration("mbillemo", PASSWORD));

        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli", demoAppWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli", demoTicketWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "fcorneli", demoPaymentWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "fcorneli",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", demoAppWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", demoTicketWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "dieter", demoPaymentWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "dieter",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", demoAppWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", demoTicketWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mario", demoPaymentWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "mario",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute", demoAppWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute", demoTicketWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "wvdhaute", demoPaymentWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "wvdhaute",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo", demoAppWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo", demoTicketWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT, "mbillemo", demoPaymentWebappName));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "mbillemo",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));

    }

    private void configSubscription(String topic, X509Certificate certificate) {

        String address = protocol + "://" + hostname + ":";
        if (protocol.equals("http")) {
            address += hostport;
        } else {
            address += hostportssl;
        }
        address += "/safe-online-demo-ws/consumer";
        notificationSubcriptions.add(new NotificationSubscription(topic, address, certificate));
    }

    private void configureNode() {

        SafeOnlineKeyStore olasKeyStore = new SafeOnlineKeyStore();
        SafeOnlineNodeKeyStore nodeKeyStore = new SafeOnlineNodeKeyStore();

        node = new Node(nodeName, sslProtocol, hostname, hostport, hostportssl, nodeKeyStore.getCertificate(),
                olasKeyStore.getCertificate());
        trustedCertificates.put(nodeKeyStore.getCertificate(), SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
    }

    @Override
    public int getPriority() {

        return BeIdConstants.BEID_BOOT_PRIORITY - 1;
    }
}
