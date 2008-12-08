/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.password.bean;

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
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.model.password.PasswordManager;
import net.link.safeonline.password.keystore.PasswordKeyStoreUtils;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = PasswordStartableBean.JNDI_BINDING)
public class PasswordStartableBean extends AbstractInitBean {

    public static final String JNDI_BINDING = PasswordConstants.PASSWORD_STARTABLE_JNDI_PREFIX + "PasswordStartableBean";

    @EJB(mappedName = PasswordManager.JNDI_BINDING)
    private PasswordManager    passwordManager;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService     subjectService;


    private static class PasswordRegistration {

        final String login;

        final String password;


        public PasswordRegistration(String login, String password) {

            this.login = login;
            this.password = password;
        }
    }


    private List<PasswordRegistration> passwordRegistrations;


    public PasswordStartableBean() {

        this.passwordRegistrations = new LinkedList<PasswordRegistration>();

        configureNode();

        configureDevice();

        configureDeviceRegistrations();

    }

    @Override
    public void postStart() {

        super.postStart();

        for (PasswordRegistration passwordRegistration : this.passwordRegistrations) {

            SubjectEntity subject = this.subjectService.findSubjectFromUserName(passwordRegistration.login);
            if (null == subject) {
                try {
                    subject = this.subjectService.addSubject(passwordRegistration.login);
                } catch (AttributeTypeNotFoundException e) {
                    this.LOG.fatal("safeonline exception", e);
                    throw new EJBException(e);
                }
            }

            if (!this.passwordManager.isPasswordConfigured(subject)) {
                try {
                    this.passwordManager.setPassword(subject, passwordRegistration.password);
                } catch (PermissionDeniedException e) {
                    throw new EJBException(e);
                }
            }
        }
    }

    private void configureNode() {

        ResourceBundle properties = ResourceBundle.getBundle("password_config");
        String nodeName = properties.getString("olas.node.name");
        String protocol = properties.getString("olas.host.protocol");
        String hostname = properties.getString("olas.host.name");
        int hostport = Integer.parseInt(properties.getString("olas.host.port"));
        int hostportssl = Integer.parseInt(properties.getString("olas.host.port.ssl"));

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        IdentityServiceClient identityServiceClient = new IdentityServiceClient();

        this.node = new Node(nodeName, protocol, hostname, hostport, hostportssl, authIdentityServiceClient.getCertificate(),
                identityServiceClient.getCertificate());
        this.trustedCertificates.put(authIdentityServiceClient.getCertificate(), SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN);
    }

    private void configureDevice() {

        X509Certificate certificate = (X509Certificate) PasswordKeyStoreUtils.getPrivateKeyEntry().getCertificate();

        ResourceBundle properties = ResourceBundle.getBundle("password_config");
        String nodeName = properties.getString("olas.node.name");
        String passwordWebappName = properties.getString("password.webapp.name");

        AttributeTypeEntity passwordHashAttributeType = new AttributeTypeEntity(PasswordConstants.PASSWORD_HASH_ATTRIBUTE,
                DatatypeType.STRING, false, false);
        AttributeTypeEntity passwordSeedAttributeType = new AttributeTypeEntity(PasswordConstants.PASSWORD_SEED_ATTRIBUTE,
                DatatypeType.STRING, false, false);
        AttributeTypeEntity passwordAlgorithmAttributeType = new AttributeTypeEntity(PasswordConstants.PASSWORD_ALGORITHM_ATTRIBUTE,
                DatatypeType.STRING, false, false);
        this.attributeTypes.add(passwordHashAttributeType);
        this.attributeTypes.add(passwordSeedAttributeType);
        this.attributeTypes.add(passwordAlgorithmAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(passwordHashAttributeType, Locale.ENGLISH.getLanguage(),
                "Password hash", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(passwordSeedAttributeType, Locale.ENGLISH.getLanguage(),
                "Password hash seed", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(passwordAlgorithmAttributeType, Locale.ENGLISH.getLanguage(),
                "Password hash algorithm", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(passwordHashAttributeType, "nl", "Wachtwoord hash", null));
        this.attributeTypeDescriptions
                                      .add(new AttributeTypeDescriptionEntity(passwordSeedAttributeType, "nl", "Wachtwoord hash seed", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(passwordAlgorithmAttributeType, "nl",
                "Wachtwoord hash algoritme", null));

        AttributeTypeEntity passwordDeviceDisableAttributeType = new AttributeTypeEntity(
                PasswordConstants.PASSWORD_DEVICE_DISABLE_ATTRIBUTE, DatatypeType.BOOLEAN, false, false);
        passwordDeviceDisableAttributeType.setMultivalued(true);
        this.attributeTypes.add(passwordDeviceDisableAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(passwordDeviceDisableAttributeType,
                Locale.ENGLISH.getLanguage(), "Password Disable Attribute", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(passwordDeviceDisableAttributeType, "nl",
                "Paswoord Disable Attribuut", null));

        AttributeTypeEntity passwordDeviceAttributeType = new AttributeTypeEntity(PasswordConstants.PASSWORD_DEVICE_ATTRIBUTE,
                DatatypeType.COMPOUNDED, false, false);
        passwordDeviceAttributeType.setMultivalued(true);
        passwordDeviceAttributeType.addMember(passwordHashAttributeType, 0, true);
        passwordDeviceAttributeType.addMember(passwordSeedAttributeType, 1, true);
        passwordDeviceAttributeType.addMember(passwordAlgorithmAttributeType, 2, true);
        passwordDeviceAttributeType.addMember(passwordDeviceDisableAttributeType, 3, true);
        this.attributeTypes.add(passwordDeviceAttributeType);
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(passwordDeviceAttributeType, Locale.ENGLISH.getLanguage(),
                "Password", null));
        this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(passwordDeviceAttributeType, "nl", "Wachtwoord", null));

        this.devices.add(new Device(PasswordConstants.PASSWORD_DEVICE_ID, SafeOnlineConstants.PASSWORD_DEVICE_CLASS, nodeName, "/"
                + passwordWebappName + "/auth", "/" + passwordWebappName + "/device", "/" + passwordWebappName + "/device", "/"
                + passwordWebappName + "/device", "/" + passwordWebappName + "/device", "/" + passwordWebappName + "/device", certificate,
                passwordDeviceAttributeType, null, passwordDeviceDisableAttributeType));
        this.deviceDescriptions.add(new DeviceDescription(PasswordConstants.PASSWORD_DEVICE_ID, "nl", "Paswoord"));
        this.deviceDescriptions.add(new DeviceDescription(PasswordConstants.PASSWORD_DEVICE_ID, Locale.ENGLISH.getLanguage(), "Password"));
        this.trustedCertificates.put(certificate, SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN);

    }

    private void configureDeviceRegistrations() {

        this.passwordRegistrations.add(new PasswordRegistration(SafeOnlineConstants.ADMIN_LOGIN, "admin"));
        this.passwordRegistrations.add(new PasswordRegistration(SafeOnlineConstants.OWNER_LOGIN, "secret"));
    }

    @Override
    public void preStop() {

        this.LOG.debug("pre stop");
    }

    @Override
    public int getPriority() {

        return PasswordConstants.PASSWORD_BOOT_PRIORITY;
    }
}
