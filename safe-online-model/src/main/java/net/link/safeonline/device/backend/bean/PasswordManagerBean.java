/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.backend.bean;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.device.backend.PasswordManager;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = PasswordManager.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class PasswordManagerBean implements PasswordManager {

    private static final String defaultHashingAlgorithm = "SHA-512";

    @EJB
    private AttributeDAO        attributeDAO;

    @EJB
    private AttributeTypeDAO    attributeTypeDAO;


    public void changePassword(SubjectEntity subject, String oldPassword, String newPassword)
            throws PermissionDeniedException, DeviceNotFoundException {

        if (isPasswordConfigured(subject)) {
            if (!validatePassword(subject, oldPassword))
                throw new PermissionDeniedException("password mismatch");
        }

        setPasswordWithForce(subject, newPassword);
    }

    public void setPassword(SubjectEntity subject, String password)
            throws PermissionDeniedException {

        if (isPasswordConfigured(subject))
            throw new PermissionDeniedException("password already configured");

        setPasswordWithForce(subject, password);
    }

    private void setPasswordWithForce(SubjectEntity subject, String password) {

        AttributeTypeEntity passwordHashAttributeType;
        AttributeTypeEntity passwordSeedAttributeType;
        AttributeTypeEntity passwordAlgorithmAttributeType;
        AttributeTypeEntity passwordDeviceAttributeType;
        AttributeTypeEntity passwordDeviceDisableAttributeType;
        try {
            passwordHashAttributeType = this.attributeTypeDAO.getAttributeType(SafeOnlineConstants.PASSWORD_HASH_ATTRIBUTE);
            passwordSeedAttributeType = this.attributeTypeDAO.getAttributeType(SafeOnlineConstants.PASSWORD_SEED_ATTRIBUTE);
            passwordAlgorithmAttributeType = this.attributeTypeDAO.getAttributeType(SafeOnlineConstants.PASSWORD_ALGORITHM_ATTRIBUTE);
            passwordDeviceAttributeType = this.attributeTypeDAO.getAttributeType(SafeOnlineConstants.PASSWORD_DEVICE_ATTRIBUTE);
            passwordDeviceDisableAttributeType = this.attributeTypeDAO
                                                                      .getAttributeType(SafeOnlineConstants.PASSWORD_DEVICE_DISABLE_ATTRIBUTE);
        } catch (AttributeTypeNotFoundException e) {
            throw new EJBException("password attribute types not found");
        }

        String seed = subject.getUserId();
        String hashValue;
        try {
            hashValue = hash(password, seed, defaultHashingAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new EJBException("Could not find the default password hashing algorithm: " + defaultHashingAlgorithm);
        }
        try {
            Password passwordAttribute = getPasswordAttribute(subject);

            passwordAttribute.hash.setStringValue(hashValue);
            passwordAttribute.seed.setStringValue(seed);
            passwordAttribute.algorithm.setStringValue(defaultHashingAlgorithm);
        } catch (DeviceNotFoundException e) {
            AttributeEntity hashAttribute = this.attributeDAO.addAttribute(passwordHashAttributeType, subject, hashValue);
            AttributeEntity seedAttribute = this.attributeDAO.addAttribute(passwordSeedAttributeType, subject, seed);
            AttributeEntity algorithmAttribute = this.attributeDAO.addAttribute(passwordAlgorithmAttributeType, subject,
                    defaultHashingAlgorithm);
            AttributeEntity disableAttribute = this.attributeDAO.addAttribute(passwordDeviceDisableAttributeType, subject);
            disableAttribute.setBooleanValue(false);
            List<AttributeEntity> members = new LinkedList<AttributeEntity>();
            members.add(hashAttribute);
            members.add(seedAttribute);
            members.add(algorithmAttribute);
            members.add(disableAttribute);
            AttributeEntity parentAttribute = this.attributeDAO.addAttribute(passwordDeviceAttributeType, subject);
            parentAttribute.setMembers(members);
        }
    }

    public boolean isDisabled(SubjectEntity subject)
            throws DeviceNotFoundException {

        Password password = getPasswordAttribute(subject);
        return password.disabled.getBooleanValue();
    }

    public boolean validatePassword(SubjectEntity subject, String password)
            throws DeviceNotFoundException {

        // get current password
        Password expectedPassword = getPasswordAttribute(subject);
        String expectedPasswordHash = expectedPassword.hash.getStringValue();
        String seed = expectedPassword.seed.getStringValue();
        String algorithm = expectedPassword.algorithm.getStringValue();

        // calculate hash
        String givenPasswordHash;
        try {
            givenPasswordHash = hash(password, seed, algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new EJBException("Password hashing algorithm not found: " + algorithm);
        }

        // compare hash
        if (expectedPasswordHash.equals(givenPasswordHash)) {
            // update hash to new default
            if (!algorithm.equals(defaultHashingAlgorithm)) {
                setPasswordWithForce(subject, password);
            }
            return true;
        }
        return false;
    }

    private Password getPasswordAttribute(SubjectEntity subject)
            throws DeviceNotFoundException {

        AttributeEntity passwordHashAttribute = this.attributeDAO.findAttribute(SafeOnlineConstants.PASSWORD_HASH_ATTRIBUTE, subject);
        AttributeEntity passwordSeedAttribute = this.attributeDAO.findAttribute(SafeOnlineConstants.PASSWORD_SEED_ATTRIBUTE, subject);
        AttributeEntity passwordAlgorithmAttribute = this.attributeDAO.findAttribute(SafeOnlineConstants.PASSWORD_ALGORITHM_ATTRIBUTE,
                subject);
        AttributeEntity passwordDisableAttribute = this.attributeDAO.findAttribute(SafeOnlineConstants.PASSWORD_DEVICE_DISABLE_ATTRIBUTE,
                subject);
        AttributeEntity passwordParentAttribute = this.attributeDAO.findAttribute(SafeOnlineConstants.PASSWORD_DEVICE_ATTRIBUTE, subject);
        if (null == passwordHashAttribute || null == passwordSeedAttribute || null == passwordAlgorithmAttribute
                || null == passwordParentAttribute || null == passwordDisableAttribute)
            throw new DeviceNotFoundException();
        String hash = passwordHashAttribute.getStringValue();
        String seed = passwordSeedAttribute.getStringValue();
        String algorithm = passwordAlgorithmAttribute.getStringValue();
        if (null == hash || null == seed || null == algorithm)
            throw new DeviceNotFoundException();

        return new Password(passwordHashAttribute, passwordSeedAttribute, passwordAlgorithmAttribute, passwordDisableAttribute,
                passwordParentAttribute);
    }

    public boolean isPasswordConfigured(SubjectEntity subject) {

        try {
            getPasswordAttribute(subject);
        } catch (DeviceNotFoundException e) {
            return false;
        }
        return true;
    }

    public void removePassword(SubjectEntity subject, String password)
            throws DeviceNotFoundException, PermissionDeniedException {

        if (!validatePassword(subject, password))
            throw new PermissionDeniedException("password mismatch");

        Password currentPassword = getPasswordAttribute(subject);
        this.attributeDAO.removeAttribute(currentPassword.algorithm);
        this.attributeDAO.removeAttribute(currentPassword.hash);
        this.attributeDAO.removeAttribute(currentPassword.seed);
        this.attributeDAO.removeAttribute(currentPassword.parent);
    }

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void disablePassword(SubjectEntity subject, boolean disable)
            throws DeviceNotFoundException {

        Password password = getPasswordAttribute(subject);
        password.disabled.setBooleanValue(disable);

    }


    private static class Password {

        public AttributeEntity hash;
        public AttributeEntity seed;
        public AttributeEntity algorithm;
        public AttributeEntity disabled;
        public AttributeEntity parent;


        public Password(AttributeEntity hash, AttributeEntity seed, AttributeEntity algorithm, AttributeEntity disabled,
                        AttributeEntity parent) {

            this.hash = hash;
            this.seed = seed;
            this.algorithm = algorithm;
            this.disabled = disabled;
            this.parent = parent;
        }
    }


    private static String hash(String input, String seed, String algorithm)
            throws NoSuchAlgorithmException {

        String toHash = input + seed;
        byte[] plainText = null;

        try {
            plainText = toHash.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new EJBException("Unsupported encoding in password hash function");
        }

        MessageDigest messageDigest = MessageDigest.getInstance(algorithm, new BouncyCastleProvider());

        messageDigest.update(plainText);
        String digestAsString = new sun.misc.BASE64Encoder().encode(messageDigest.digest());

        return digestAsString;
    }

}
