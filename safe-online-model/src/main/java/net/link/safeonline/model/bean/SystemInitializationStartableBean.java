/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.helpdesk.keystore.HelpdeskKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.oper.keystore.OperKeyStore;
import net.link.safeonline.owner.keystore.OwnerKeyStore;
import net.link.safeonline.user.keystore.UserKeyStore;
import net.link.safeonline.util.servlet.SafeOnlineConfig;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * This component will initialize the system at startup.
 * 
 * For now it creates initial users, applications and subscriptions. This to allow for admins to gain access to the system and thus to
 * bootstrap the SafeOnline core.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = SystemInitializationStartableBean.JNDI_BINDING)
public class SystemInitializationStartableBean extends AbstractInitBean {

    public static final String JNDI_BINDING = Startable.JNDI_PREFIX + "SystemInitializationStartableBean";


    @Override
    public void postStart() {

        // Load OLAS configuration.
        ResourceBundle properties = ResourceBundle.getBundle("config");
        String userWebappName = properties.getString("olas.user.webapp.name");
        String operWebappName = properties.getString("olas.oper.webapp.name");
        String ownerWebappName = properties.getString("olas.owner.webapp.name");
        String helpdeskWebappName = properties.getString("olas.helpdesk.webapp.name");

        // Initialize this OLAS node, common attributes and available devices.
        configureNode();
        configureAttributeTypes();
        configureDevices();

        // Add some initial users.
        adminUsers.add(SafeOnlineConstants.ADMIN_LOGIN);
        applicationOwnersAndLogin.put(SafeOnlineConstants.OWNER_LOGIN, SafeOnlineConstants.OWNER_LOGIN);

        // Add the core applications.
        operatorApplicationName = SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME;
        userApplicationName = SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME;
        ownerApplicationName = SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME;
        helpdeskApplicationName = SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME;

        X509Certificate userCert = (X509Certificate) UserKeyStore.getPrivateKeyEntry().getCertificate();
        X509Certificate operCert = (X509Certificate) OperKeyStore.getPrivateKeyEntry().getCertificate();
        X509Certificate ownerCert = (X509Certificate) OwnerKeyStore.getPrivateKeyEntry().getCertificate();
        X509Certificate helpdeskCert = (X509Certificate) HelpdeskKeyStore.getPrivateKeyEntry().getCertificate();

        try {
            URL userApplicationUrl = new URL(SafeOnlineConfig.nodeProtocol(), SafeOnlineConfig.nodeHost(), SafeOnlineConfig.nodePort(), "/"
                    + userWebappName);
            URL userSSOLogoutUrl = new URL(SafeOnlineConfig.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
                    SafeOnlineConfig.nodePortSecure(), "/" + userWebappName + "/logout");

            registeredApplications.add(new Application(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
                    SafeOnlineConstants.OWNER_LOGIN, "The SafeOnline User Web Application.", //
                    userApplicationUrl, getLogo("/logo.png"), false, false, userCert, false, IdScopeType.USER, true, userSSOLogoutUrl));

            URL operApplicationUrl = new URL(SafeOnlineConfig.nodeProtocol(), SafeOnlineConfig.nodeHost(), SafeOnlineConfig.nodePort(), "/"
                    + operWebappName);
            URL operSSOLogoutUrl = new URL(SafeOnlineConfig.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
                    SafeOnlineConfig.nodePortSecure(), "/" + operWebappName + "/logout");
            registeredApplications.add(new Application(SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME,
                    SafeOnlineConstants.OWNER_LOGIN, "The SafeOnline Operator Web Application.", operApplicationUrl, getLogo("/logo.png"),
                    false, false, operCert, false, IdScopeType.USER, true, operSSOLogoutUrl));

            URL ownerApplicationUrl = new URL(SafeOnlineConfig.nodeProtocol(), SafeOnlineConfig.nodeHost(), SafeOnlineConfig.nodePort(),
                    "/" + ownerWebappName);
            URL ownerSSOLogoutUrl = new URL(SafeOnlineConfig.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
                    SafeOnlineConfig.nodePortSecure(), "/" + ownerWebappName + "/logout");
            registeredApplications.add(new Application(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME,
                    SafeOnlineConstants.OWNER_LOGIN, "The SafeOnline Application Owner Web Application.", ownerApplicationUrl,
                    getLogo("/logo.png"), false, false, ownerCert, false, IdScopeType.USER, true, ownerSSOLogoutUrl));

            URL helpdeskApplicationUrl = new URL(SafeOnlineConfig.nodeProtocol(), SafeOnlineConfig.nodeHost(), SafeOnlineConfig.nodePort(),
                    "/" + helpdeskWebappName);
            URL helpdeskSSOLogoutUrl = new URL(SafeOnlineConfig.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
                    SafeOnlineConfig.nodePortSecure(), "/" + helpdeskWebappName + "/logout");
            registeredApplications.add(new Application(SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME,
                    SafeOnlineConstants.OWNER_LOGIN, "The SafeOnline Helpdesk Web Application.", helpdeskApplicationUrl,
                    getLogo("/logo.png"), false, false, helpdeskCert, false, IdScopeType.USER, true, helpdeskSSOLogoutUrl));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed Application URL exception: " + e.getMessage());
        }

        trustedCertificates.put(userCert, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        trustedCertificates.put(operCert, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        trustedCertificates.put(ownerCert, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        trustedCertificates.put(helpdeskCert, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);

        // add available notification topics
        notificationTopics.add(SafeOnlineConstants.TOPIC_REMOVE_USER);
        notificationTopics.add(SafeOnlineConstants.TOPIC_UNSUBSCRIBE_USER);

        // now initialize
        super.postStart();

    }

    private void configureNode() {

        SafeOnlineNodeKeyStore nodeKeyStore = new SafeOnlineNodeKeyStore();

        node = new Node(SafeOnlineConfig.nodeName(), SafeOnlineConfig.nodeProtocolSecure(), SafeOnlineConfig.nodeHost(),
                SafeOnlineConfig.nodePort(), SafeOnlineConfig.nodePortSecure(), nodeKeyStore.getCertificate());
        trustedCertificates.put(nodeKeyStore.getCertificate(), SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
    }

    private void configureAttributeTypes() {

        AttributeTypeEntity loginAttributeType = new AttributeTypeEntity(SafeOnlineConstants.LOGIN_ATTRIBTUE, DatatypeType.STRING, false,
                false);
        attributeTypes.add(loginAttributeType);
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(loginAttributeType, Locale.ENGLISH.getLanguage(), "Login name",
                null));
        attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(loginAttributeType, "nl", "Login naam", null));
    }

    private void configureDevices() {

        deviceClasses
                     .add(new DeviceClass(SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS));
        deviceClasses.add(new DeviceClass(SafeOnlineConstants.MOBILE_DEVICE_CLASS, SafeOnlineConstants.MOBILE_DEVICE_AUTH_CONTEXT_CLASS));
        deviceClasses.add(new DeviceClass(SafeOnlineConstants.PKI_DEVICE_CLASS, SafeOnlineConstants.PKI_DEVICE_AUTH_CONTEXT_CLASS));
        deviceClasses
                     .add(new DeviceClass(SafeOnlineConstants.DIGIPASS_DEVICE_CLASS, SafeOnlineConstants.DIGIPASS_DEVICE_AUTH_CONTEXT_CLASS));
    }

    @Override
    public int getPriority() {

        return Startable.PRIORITY_BOOTSTRAP;
    }
}
