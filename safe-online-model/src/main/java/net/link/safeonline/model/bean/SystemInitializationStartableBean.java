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
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.helpdesk.keystore.HelpdeskKeyStore;
import net.link.safeonline.keystore.SafeOnlineKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.entity.Type;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.oper.keystore.OperKeyStore;
import net.link.safeonline.owner.keystore.OwnerKeyStore;
import net.link.safeonline.user.keystore.UserKeyStore;
import net.link.safeonline.util.ee.EjbUtils;

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

    public static final String  JNDI_BINDING      = Startable.JNDI_PREFIX + "SystemInitializationStartableBean";
    private static final String OLAS_KEY_PASSWORD = "secret";
    private static final Object NODE_KEY_PASSWORD = "secret";
    private static final String OLAS_KEY_RESOURCE = "safe-online-keystore.jks";
    private static final String NODE_KEY_RESOURCE = "safe-online-node-keystore.jks";


    public SystemInitializationStartableBean() {

        // Load OLAS configuration.
        ResourceBundle properties = ResourceBundle.getBundle("config");
        String protocol = properties.getString("olas.host.protocol");
        String protocolssl = properties.getString("olas.host.protocol.ssl");
        String hostname = properties.getString("olas.host.name");
        int hostport = Integer.parseInt(properties.getString("olas.host.port"));
        int hostportssl = Integer.parseInt(properties.getString("olas.host.port.ssl"));
        String userWebappName = properties.getString("olas.user.webapp.name");
        String operWebappName = properties.getString("olas.oper.webapp.name");
        String ownerWebappName = properties.getString("olas.owner.webapp.name");
        String helpdeskWebappName = properties.getString("olas.helpdesk.webapp.name");

        // Initialize this OLAS node, common attributes and available devices.
        configureKeys();
        configureNode();
        configureAttributeTypes();
        configureDevices();

        // Add some initial users.
        users.add(SafeOnlineConstants.ADMIN_LOGIN);
        users.add(SafeOnlineConstants.OWNER_LOGIN);
        applicationOwnersAndLogin.put(SafeOnlineConstants.OWNER_LOGIN, SafeOnlineConstants.OWNER_LOGIN);

        // Add the core applications.
        X509Certificate userCert = (X509Certificate) UserKeyStore.getPrivateKeyEntry().getCertificate();
        X509Certificate operCert = (X509Certificate) OperKeyStore.getPrivateKeyEntry().getCertificate();
        X509Certificate ownerCert = (X509Certificate) OwnerKeyStore.getPrivateKeyEntry().getCertificate();
        X509Certificate helpdeskCert = (X509Certificate) HelpdeskKeyStore.getPrivateKeyEntry().getCertificate();

        try {
            registeredApplications.add(new Application(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, "owner",
                    "The SafeOnline User Web Application.", new URL(protocol, hostname, hostport, "/" + userWebappName),
                    getLogo("/logo.jpg"), false, false, userCert, false, IdScopeType.USER, true, new URL(protocolssl, hostname,
                            hostportssl, "/" + userWebappName + "/logout")));
            registeredApplications.add(new Application(SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME, "owner",
                    "The SafeOnline Operator Web Application.", new URL(protocol, hostname, hostport, "/" + operWebappName),
                    getLogo("/logo.jpg"), false, false, operCert, false, IdScopeType.USER, true, new URL(protocolssl, hostname,
                            hostportssl, "/" + operWebappName + "/logout")));
            registeredApplications.add(new Application(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME, "owner",
                    "The SafeOnline Application Owner Web Application.", new URL(protocol, hostname, hostport, "/" + ownerWebappName),
                    getLogo("/logo.jpg"), false, false, ownerCert, false, IdScopeType.USER, true, new URL(protocolssl, hostname,
                            hostportssl, "/" + ownerWebappName + "/logout")));
            registeredApplications.add(new Application(SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME, "owner",
                    "The SafeOnline Helpdesk Web Application.", new URL(protocol, hostname, hostport, "/" + helpdeskWebappName),
                    getLogo("/logo.jpg"), false, false, helpdeskCert, false, IdScopeType.USER, true, new URL(protocolssl, hostname,
                            hostportssl, "/" + helpdeskWebappName + "/logout")));
        } catch (MalformedURLException e) {
            throw new EJBException("Malformed Application URL exception: " + e.getMessage());
        }

        trustedCertificates.put(userCert, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        trustedCertificates.put(operCert, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        trustedCertificates.put(ownerCert, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
        trustedCertificates.put(helpdeskCert, SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);

        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "admin",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "admin",
                SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "admin",
                SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME));

        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "owner",
                SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME));
        subscriptions.add(new Subscription(SubscriptionOwnerType.APPLICATION, "owner",
                SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME));

        // add available notification topics
        notificationTopics.add(SafeOnlineConstants.TOPIC_REMOVE_USER);
        notificationTopics.add(SafeOnlineConstants.TOPIC_UNSUBSCRIBE_USER);

    }

    private void configureKeys() {

        KeyService keyService = EjbUtils.getEJB(KeyService.JNDI_BINDING, KeyService.class);
        if (keyService.getConfig(SafeOnlineKeyStore.class) == null) {
            keyService.configure(SafeOnlineKeyStore.class, Type.JKS, String.format("%s:%s:%s", OLAS_KEY_PASSWORD, OLAS_KEY_PASSWORD,
                    OLAS_KEY_RESOURCE));
        }
        if (keyService.getConfig(SafeOnlineNodeKeyStore.class) == null) {
            keyService.configure(SafeOnlineNodeKeyStore.class, Type.JKS, String.format("%s:%s:%s", NODE_KEY_PASSWORD, NODE_KEY_PASSWORD,
                    NODE_KEY_RESOURCE));
        }
    }

    private void configureNode() {

        ResourceBundle properties = ResourceBundle.getBundle("config");
        String nodeName = properties.getString("olas.node.name");
        String sslprotocol = properties.getString("olas.host.protocol.ssl");
        String hostname = properties.getString("olas.host.name");
        int hostport = Integer.parseInt(properties.getString("olas.host.port"));
        int hostportssl = Integer.parseInt(properties.getString("olas.host.port.ssl"));

        SafeOnlineKeyStore olasKeyStore = new SafeOnlineKeyStore();
        SafeOnlineNodeKeyStore nodeKeyStore = new SafeOnlineNodeKeyStore();

        node = new Node(nodeName, sslprotocol, hostname, hostport, hostportssl, nodeKeyStore.getCertificate(),
                olasKeyStore.getCertificate());
        trustedCertificates.put(nodeKeyStore.getCertificate(), SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
        trustedCertificates.put(olasKeyStore.getCertificate(), SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
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
