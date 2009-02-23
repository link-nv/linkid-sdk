/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.password.bean;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.data.CompoundAttributeDO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.model.password.PasswordManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = PasswordManager.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class PasswordManagerBean implements PasswordManager {

    private static final String    defaultHashingAlgorithm = "SHA-512";

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        attributeManager = new AttributeManagerLWBean(attributeDAO, attributeTypeDAO);
    }

    /**
     * {@inheritDoc}
     */
    public void updatePassword(SubjectEntity subject, String oldPassword, String newPassword)
            throws DeviceRegistrationNotFoundException, DeviceAuthenticationException {

        if (!validatePassword(subject, oldPassword))
            throw new DeviceAuthenticationException("password mismatch");

        setPasswordWithForce(subject, newPassword);
    }

    /**
     * {@inheritDoc}
     */
    public void registerPassword(SubjectEntity subject, String password) {

        try {
            String seed = subject.getUserId();
            String hashValue = hash(password, seed, defaultHashingAlgorithm);

            CompoundAttributeDO deviceAttribute = attributeManager.newCompound(PasswordConstants.PASSWORD_DEVICE_ATTRIBUTE, subject);
            deviceAttribute.addAttribute(PasswordConstants.PASSWORD_HASH_ATTRIBUTE, hashValue);
            deviceAttribute.addAttribute(PasswordConstants.PASSWORD_SEED_ATTRIBUTE, seed);
            deviceAttribute.addAttribute(PasswordConstants.PASSWORD_ALGORITHM_ATTRIBUTE, defaultHashingAlgorithm);
            deviceAttribute.addAttribute(PasswordConstants.PASSWORD_NEW_ALGORITHM_ATTRIBUTE, null);
            deviceAttribute.addAttribute(PasswordConstants.PASSWORD_DEVICE_DISABLE_ATTRIBUTE, false);
        }

        catch (NoSuchAlgorithmException e) {
            throw new EJBException("Could not use the default otp over sms pin hashing algorithm: " + defaultHashingAlgorithm);
        } catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for OtpOverSMS device not defined.", e);
        }
    }

    private void setPasswordWithForce(SubjectEntity subject, String password)
            throws DeviceRegistrationNotFoundException {

        try {
            AttributeEntity deviceAttribute = attributeDAO.findAttribute(subject, PasswordConstants.PASSWORD_DEVICE_ATTRIBUTE, 0);
            if (deviceAttribute == null)
                throw new DeviceRegistrationNotFoundException("There is no device attribute type for the password device.");

            AttributeEntity algorithmAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    PasswordConstants.PASSWORD_ALGORITHM_ATTRIBUTE);
            AttributeEntity newAlgorithmAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    PasswordConstants.PASSWORD_NEW_ALGORITHM_ATTRIBUTE);
            AttributeEntity hashAttribute = attributeManager.getCompoundMember(deviceAttribute, PasswordConstants.PASSWORD_HASH_ATTRIBUTE);
            AttributeEntity seedAttribute = attributeManager.getCompoundMember(deviceAttribute, PasswordConstants.PASSWORD_SEED_ATTRIBUTE);

            String seed = subject.getUserId();
            String algorithm = newAlgorithmAttribute.isEmpty()? algorithmAttribute.getStringValue(): newAlgorithmAttribute.getStringValue();
            String hashValue = hash(password, seed, algorithm);

            hashAttribute.setValue(hashValue);
            seedAttribute.setValue(seed);

            if (false == newAlgorithmAttribute.isEmpty()) {
                // We used newAlgorithm, write it to algorithm and unset newAlgorithm.
                algorithmAttribute.setValue(algorithm);
                newAlgorithmAttribute.setValue(null);
            }
        }

        catch (NoSuchAlgorithmException e) {
            throw new EJBException("Could not find the hashing algorithm.");
        } catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for OtpOverSMS device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean validatePassword(SubjectEntity subject, String password)
            throws DeviceRegistrationNotFoundException {

        try {
            AttributeEntity deviceAttribute = attributeDAO.findAttribute(subject, PasswordConstants.PASSWORD_DEVICE_ATTRIBUTE, 0);
            if (deviceAttribute == null)
                throw new DeviceRegistrationNotFoundException("There is no device attribute type for the password device.");

            AttributeEntity hashAttribute = attributeManager.getCompoundMember(deviceAttribute, PasswordConstants.PASSWORD_HASH_ATTRIBUTE);
            AttributeEntity seedAttribute = attributeManager.getCompoundMember(deviceAttribute, PasswordConstants.PASSWORD_SEED_ATTRIBUTE);
            AttributeEntity algorithmAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    PasswordConstants.PASSWORD_ALGORITHM_ATTRIBUTE);
            AttributeEntity newAlgorithmAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    PasswordConstants.PASSWORD_NEW_ALGORITHM_ATTRIBUTE);

            // calculate hash
            String givenPinHash = hash(password, seedAttribute.getStringValue(), algorithmAttribute.getStringValue());

            // compare hash
            if (hashAttribute.getStringValue().equals(givenPinHash)) {
                // In case we need to update the hash to a new algorithm:
                if (false == newAlgorithmAttribute.isEmpty()) {
                    setPasswordWithForce(subject, password);
                }

                return true;
            }

            return false;
        }

        catch (NoSuchAlgorithmException e) {
            throw new EJBException("Pin hashing algorithm not found.");
        } catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for OtpOverSMS device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removePassword(SubjectEntity subject) {

        AttributeEntity deviceAttribute = attributeDAO.findAttribute(subject, PasswordConstants.PASSWORD_DEVICE_ATTRIBUTE, 0);
        attributeManager.removeCompoundAttribute(deviceAttribute);
    }

    private static String hash(String input, String seed, String algorithm)
            throws NoSuchAlgorithmException {

        try {
            String toHash = input + seed;
            byte[] plainText = toHash.getBytes("UTF8");

            MessageDigest messageDigest = MessageDigest.getInstance(algorithm, new BouncyCastleProvider());
            messageDigest.update(plainText);

            return new String(Base64.encode(messageDigest.digest()));
        }

        catch (UnsupportedEncodingException e) {
            throw new EJBException("Unsupported encoding in mobile pin hash function");
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPasswordConfigured(SubjectEntity subject) {

        AttributeEntity deviceAttribute = attributeDAO.findAttribute(subject, PasswordConstants.PASSWORD_DEVICE_ATTRIBUTE, 0);

        return deviceAttribute != null;
    }
}
