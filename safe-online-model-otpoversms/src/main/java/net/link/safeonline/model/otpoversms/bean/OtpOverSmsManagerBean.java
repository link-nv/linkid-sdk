/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms.bean;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;
import net.link.safeonline.model.otpoversms.OtpOverSmsManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = OtpOverSmsManager.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class, ConfigurationInterceptor.class })
@Configurable
public class OtpOverSmsManagerBean implements OtpOverSmsManager {

    private static final Log       LOG                     = LogFactory.getLog(OtpOverSmsManagerBean.class);

    private static final String    defaultHashingAlgorithm = "SHA-512";

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO              deviceDAO;

    @Configurable(name = "Maximum Pin Attempts", group = "OTP over SMS")
    private Integer                configAttempts          = 3;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        attributeManager = new AttributeManagerLWBean(attributeDAO);
    }

    public boolean changePin(SubjectEntity subject, String mobile, String oldPin, String newPin)
            throws DeviceNotFoundException {

        if (!validatePin(subject, mobile, oldPin))
            return false;

        setPinWithForce(subject, mobile, newPin);

        return true;
    }

    private void setPinWithForce(SubjectEntity subject, String mobile, String pin)
            throws DeviceNotFoundException {

        Mobile mobileAttribute = getMobileAttribute(subject, mobile);

        String seed = subject.getUserId();
        String hashValue;
        try {
            hashValue = hash(pin, seed, defaultHashingAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new EJBException("Could not find the default otp over sms pin hashing algorithm: " + defaultHashingAlgorithm);
        }

        mobileAttribute.hash.setStringValue(hashValue);
    }

    public void registerMobile(SubjectEntity subject, String mobile, String pin)
            throws PermissionDeniedException {

        AttributeTypeEntity mobileAttributeType;
        AttributeTypeEntity pinHashAttributeType;
        AttributeTypeEntity pinSeedAttributeType;
        AttributeTypeEntity pinAlgorithmAttributeType;
        AttributeTypeEntity pinAttemptsAttributeType;
        AttributeTypeEntity otpOverSmsDeviceAttributeType;
        AttributeTypeEntity otpOverSmsDeviceDisableAttributeType;
        try {
            mobileAttributeType = attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE);
            pinHashAttributeType = attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_PIN_HASH_ATTRIBUTE);
            pinSeedAttributeType = attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_PIN_SEED_ATTRIBUTE);
            pinAlgorithmAttributeType = attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_PIN_ALGORITHM_ATTRIBUTE);
            pinAttemptsAttributeType = attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_PIN_ATTEMPTS_ATTRIBUTE);

            otpOverSmsDeviceAttributeType = attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE);
            otpOverSmsDeviceDisableAttributeType = attributeTypeDAO
                                                                        .getAttributeType(OtpOverSmsConstants.OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE);
        } catch (AttributeTypeNotFoundException e) {
            throw new EJBException("otp over sms attribute types not found");
        }

        String seed = subject.getUserId();
        String hashValue;
        try {
            hashValue = hash(pin, seed, defaultHashingAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new EJBException("Could not find the default otp over sms pin hashing algorithm: " + defaultHashingAlgorithm);
        }

        AttributeEntity mobileAttribute = attributeDAO.addAttribute(mobileAttributeType, subject);
        mobileAttribute.setStringValue(mobile);
        AttributeEntity hashAttribute = attributeDAO.addAttribute(pinHashAttributeType, subject);
        hashAttribute.setStringValue(hashValue);
        AttributeEntity seedAttribute = attributeDAO.addAttribute(pinSeedAttributeType, subject);
        seedAttribute.setStringValue(seed);
        AttributeEntity algorithmAttribute = attributeDAO.addAttribute(pinAlgorithmAttributeType, subject);
        algorithmAttribute.setStringValue(defaultHashingAlgorithm);
        AttributeEntity attemptsAttribute = attributeDAO.addAttribute(pinAttemptsAttributeType, subject);
        attemptsAttribute.setIntegerValue(0);
        AttributeEntity disableAttribute = attributeDAO.addAttribute(otpOverSmsDeviceDisableAttributeType, subject);
        disableAttribute.setBooleanValue(false);
        List<AttributeEntity> members = new LinkedList<AttributeEntity>();
        members.add(mobileAttribute);
        members.add(hashAttribute);
        members.add(seedAttribute);
        members.add(algorithmAttribute);
        members.add(disableAttribute);
        AttributeEntity parentAttribute = attributeDAO.addAttribute(otpOverSmsDeviceAttributeType, subject);
        parentAttribute.setMembers(members);
    }

    public boolean validatePin(SubjectEntity subject, String mobile, String pin)
            throws DeviceNotFoundException {

        // get mobile attributes
        Mobile expectedMobile = getMobileAttribute(subject, mobile);
        String expectedPinHash = expectedMobile.hash.getStringValue();
        String seed = expectedMobile.seed.getStringValue();
        String algorithm = expectedMobile.algorithm.getStringValue();

        // calculate hash
        String givenPinHash;
        try {
            givenPinHash = hash(pin, seed, algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new EJBException("Pin hashing algorithm not found: " + algorithm);
        }

        // compare hash
        if (expectedPinHash.equals(givenPinHash)) {
            // update hash to new default
            if (!algorithm.equals(defaultHashingAlgorithm)) {
                setPinWithForce(subject, mobile, pin);
            }
            return true;
        }

        addAttempt(subject, mobile);

        return false;
    }

    private void addAttempt(SubjectEntity subject, String mobile)
            throws DeviceNotFoundException {

        Mobile mobileAttribute = getMobileAttribute(subject, mobile);
        mobileAttribute.attempts.setIntegerValue(mobileAttribute.attempts.getIntegerValue() + 1);
        if (mobileAttribute.attempts.getIntegerValue() >= configAttempts) {
            mobileAttribute.disabled.setBooleanValue(true);
        }
        LOG.debug("attempts: " + mobileAttribute.attempts.getIntegerValue());
    }

    public void removeMobile(SubjectEntity subject, String mobile)
            throws DeviceNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException {

        AttributeTypeEntity deviceAttributeType = attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE);
        AttributeTypeEntity mobileAttributeType = attributeTypeDAO.getAttributeType(OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE);

        List<AttributeEntity> deviceAttributes = attributeDAO.listAttributes(subject, deviceAttributeType);
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = attributeDAO.findAttribute(subject, mobileAttributeType,
                    deviceAttribute.getAttributeIndex());
            if (mobileAttribute.getStringValue().equals(mobile)) {
                attributeManager.removeAttribute(deviceAttributeType, deviceAttribute.getAttributeIndex(), subject);
                return;
            }
        }
        return;
    }

    private static String hash(String input, String seed, String algorithm)
            throws NoSuchAlgorithmException {

        String toHash = input + seed;
        byte[] plainText = null;

        try {
            plainText = toHash.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new EJBException("Unsupported encoding in mobile pin hash function");
        }

        MessageDigest messageDigest = MessageDigest.getInstance(algorithm, new BouncyCastleProvider());

        messageDigest.update(plainText);
        String digestAsString = new sun.misc.BASE64Encoder().encode(messageDigest.digest());

        return digestAsString;
    }


    private static class Mobile {

        public AttributeEntity mobile;
        public AttributeEntity hash;
        public AttributeEntity seed;
        public AttributeEntity algorithm;
        public AttributeEntity attempts;
        public AttributeEntity disabled;
        public AttributeEntity parent;


        public Mobile(AttributeEntity mobile, AttributeEntity hash, AttributeEntity seed, AttributeEntity algorithm,
                      AttributeEntity attempts, AttributeEntity disabled, AttributeEntity parent) {

            this.mobile = mobile;
            this.hash = hash;
            this.seed = seed;
            this.algorithm = algorithm;
            this.attempts = attempts;
            this.disabled = disabled;
            this.parent = parent;
        }
    }


    private Mobile getMobileAttribute(SubjectEntity subject, String mobile)
            throws DeviceNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID);

        List<AttributeEntity> deviceAttributes = attributeDAO.listAttributes(subject, device.getAttributeType());
        for (AttributeEntity deviceAttribute : deviceAttributes) {
            AttributeEntity mobileAttribute = attributeDAO.findAttribute(subject, OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE,
                    deviceAttribute.getAttributeIndex());
            if (null == mobileAttribute)
                throw new DeviceNotFoundException();
            if (mobileAttribute.getStringValue().equals(mobile)) {
                AttributeEntity pinHashAttribute = attributeDAO.findAttribute(subject,
                        OtpOverSmsConstants.OTPOVERSMS_PIN_HASH_ATTRIBUTE, deviceAttribute.getAttributeIndex());
                AttributeEntity pinSeedAttribute = attributeDAO.findAttribute(subject,
                        OtpOverSmsConstants.OTPOVERSMS_PIN_SEED_ATTRIBUTE, deviceAttribute.getAttributeIndex());
                AttributeEntity pinAlgorithmAttribute = attributeDAO.findAttribute(subject,
                        OtpOverSmsConstants.OTPOVERSMS_PIN_ALGORITHM_ATTRIBUTE, deviceAttribute.getAttributeIndex());
                AttributeEntity pinAttemptsAttribute = attributeDAO.findAttribute(subject,
                        OtpOverSmsConstants.OTPOVERSMS_PIN_ATTEMPTS_ATTRIBUTE, deviceAttribute.getAttributeIndex());
                AttributeEntity otpOverSmsDisableAttribute = attributeDAO.findAttribute(subject,
                        OtpOverSmsConstants.OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE, deviceAttribute.getAttributeIndex());
                if (null == pinHashAttribute || null == pinSeedAttribute || null == pinAlgorithmAttribute
                        || null == otpOverSmsDisableAttribute || null == pinAttemptsAttribute)
                    throw new DeviceNotFoundException();
                String hash = pinHashAttribute.getStringValue();
                String seed = pinSeedAttribute.getStringValue();
                String algorithm = pinAlgorithmAttribute.getStringValue();
                if (null == hash || null == seed || null == algorithm)
                    throw new DeviceNotFoundException();

                return new Mobile(mobileAttribute, pinHashAttribute, pinSeedAttribute, pinAlgorithmAttribute, pinAttemptsAttribute,
                        otpOverSmsDisableAttribute, deviceAttribute);
            }
        }
        return null;
    }

}
