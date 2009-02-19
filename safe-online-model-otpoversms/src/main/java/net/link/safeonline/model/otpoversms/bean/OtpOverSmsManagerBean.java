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

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.data.CompoundAttributeDO;
import net.link.safeonline.entity.AttributeEntity;
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

    @Configurable(name = "Maximum Pin Attempts", group = "OTP over SMS")
    private Integer                configAttempts          = 3;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        attributeManager = new AttributeManagerLWBean(attributeDAO, attributeTypeDAO);
    }

    public void updatePin(SubjectEntity subject, String mobile, String oldPin, String newPin)
            throws DeviceRegistrationNotFoundException {

        setPinWithForce(subject, mobile, newPin);
    }

    private void setPinWithForce(SubjectEntity subject, String mobile, String pin)
            throws DeviceRegistrationNotFoundException {

        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE,
                    OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE, mobile);
            AttributeEntity algorithmAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_PIN_ALGORITHM_ATTRIBUTE);
            AttributeEntity newAlgorithmAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_PIN_NEW_ALGORITHM_ATTRIBUTE);
            AttributeEntity hashAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_PIN_HASH_ATTRIBUTE);
            AttributeEntity seedAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_PIN_SEED_ATTRIBUTE);

            String seed = subject.getUserId();
            String algorithm = newAlgorithmAttribute.isEmpty()? algorithmAttribute.getStringValue(): newAlgorithmAttribute.getStringValue();
            String hashValue = hash(pin, seed, algorithm);

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
            throw new DeviceRegistrationNotFoundException();
        }
    }

    public void registerMobile(SubjectEntity subject, String mobile, String pin)
            throws PermissionDeniedException {

        try {
            String seed = subject.getUserId();
            String hashValue = hash(pin, seed, defaultHashingAlgorithm);

            CompoundAttributeDO deviceAttribute = attributeManager.newCompound(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE, subject);
            deviceAttribute.addAttribute(OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE, mobile);
            deviceAttribute.addAttribute(OtpOverSmsConstants.OTPOVERSMS_PIN_HASH_ATTRIBUTE, hashValue);
            deviceAttribute.addAttribute(OtpOverSmsConstants.OTPOVERSMS_PIN_SEED_ATTRIBUTE, seed);
            deviceAttribute.addAttribute(OtpOverSmsConstants.OTPOVERSMS_PIN_ALGORITHM_ATTRIBUTE, defaultHashingAlgorithm);
            deviceAttribute.addAttribute(OtpOverSmsConstants.OTPOVERSMS_PIN_NEW_ALGORITHM_ATTRIBUTE, null);
            deviceAttribute.addAttribute(OtpOverSmsConstants.OTPOVERSMS_PIN_ATTEMPTS_ATTRIBUTE, 0);
            deviceAttribute.addAttribute(OtpOverSmsConstants.OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE, false);
        }

        catch (NoSuchAlgorithmException e) {
            throw new EJBException("Could not use the default otp over sms pin hashing algorithm: " + defaultHashingAlgorithm);
        } catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for OtpOverSMS device not defined.", e);
        }
    }

    public boolean validatePin(SubjectEntity subject, String mobile, String pin)
            throws DeviceRegistrationNotFoundException {

        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE,
                    OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE, mobile);
            AttributeEntity hashAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_PIN_HASH_ATTRIBUTE);
            AttributeEntity seedAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_PIN_SEED_ATTRIBUTE);
            AttributeEntity algorithmAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_PIN_ALGORITHM_ATTRIBUTE);
            AttributeEntity newAlgorithmAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_PIN_NEW_ALGORITHM_ATTRIBUTE);

            // calculate hash
            String givenPinHash = hash(pin, seedAttribute.getStringValue(), algorithmAttribute.getStringValue());

            // compare hash
            if (hashAttribute.getStringValue().equals(givenPinHash)) {
                // In case we need to update the hash to a new algorithm:
                if (false == newAlgorithmAttribute.isEmpty()) {
                    setPinWithForce(subject, mobile, pin);
                }

                return true;
            }

            addAttempt(subject, mobile);
            return false;
        }

        catch (NoSuchAlgorithmException e) {
            throw new EJBException("Pin hashing algorithm not found.");
        } catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for OtpOverSMS device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException();
        }
    }

    private void addAttempt(SubjectEntity subject, String mobile)
            throws DeviceRegistrationNotFoundException {

        try {
            AttributeEntity deviceAttribute = attributeManager.getCompoundWhere(subject, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE,
                    OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE, mobile);
            AttributeEntity attemptsAttribute = attributeManager.getCompoundMember(deviceAttribute,
                    OtpOverSmsConstants.OTPOVERSMS_PIN_ATTEMPTS_ATTRIBUTE);

            attemptsAttribute.setValue(attemptsAttribute.getIntegerValue() + 1);
            LOG.debug("attempts: " + attemptsAttribute.getIntegerValue());

            if (attemptsAttribute.getIntegerValue() >= configAttempts) {
                LOG.debug(" -> device disabled.");

                AttributeEntity disableAttribute = attributeManager.getCompoundMember(deviceAttribute,
                        OtpOverSmsConstants.OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE);
                disableAttribute.setValue(true);
            }
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for OtpOverSMS device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException();
        }
    }

    public void removeMobile(SubjectEntity subject, String mobile)
            throws DeviceRegistrationNotFoundException {

        try {
            attributeManager.removeCompoundWhere(subject, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ATTRIBUTE,
                    OtpOverSmsConstants.OTPOVERSMS_MOBILE_ATTRIBUTE, mobile);
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for OtpOverSMS device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException();
        }
    }

    private static String hash(String input, String seed, String algorithm)
            throws NoSuchAlgorithmException {

        try {
            String toHash = input + seed;
            byte[] plainText = toHash.getBytes("UTF8");

            MessageDigest messageDigest = MessageDigest.getInstance(algorithm, new BouncyCastleProvider());
            messageDigest.update(plainText);

            return new sun.misc.BASE64Encoder().encode(messageDigest.digest());
        }

        catch (UnsupportedEncodingException e) {
            throw new EJBException("Unsupported encoding in mobile pin hash function");
        }
    }
}
