package net.link.safeonline.authentication.service.bean;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.PasswordManager;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;

@Stateless
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class PasswordManagerBean implements PasswordManager {

	private static final String defaultHashingAlgorithm = "SHA-512";

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	public void changePassword(SubjectEntity subject, String oldPassword,
			String newPassword) throws PermissionDeniedException,
			DeviceNotFoundException {

		if (!validatePassword(subject, oldPassword)) {
			throw new PermissionDeniedException("password mismatch");
		}

		setPasswordWithForce(subject, newPassword);

	}

	public void setPassword(SubjectEntity subject, String password)
			throws PermissionDeniedException {

		if (isPasswordConfigured(subject)) {
			throw new PermissionDeniedException("password already configured");
		}

		setPasswordWithForce(subject, password);
	}

	private void setPasswordWithForce(SubjectEntity subject, String password) {
		AttributeTypeEntity passwordHashAttributeType;
		AttributeTypeEntity passwordSeedAttributeType;
		AttributeTypeEntity passwordAlgorithmAttributeType;
		try {
			passwordHashAttributeType = this.attributeTypeDAO
					.getAttributeType(SafeOnlineConstants.PASSWORD_HASH_ATTRIBUTE);
			passwordSeedAttributeType = this.attributeTypeDAO
					.getAttributeType(SafeOnlineConstants.PASSWORD_SEED_ATTRIBUTE);
			passwordAlgorithmAttributeType = this.attributeTypeDAO
					.getAttributeType(SafeOnlineConstants.PASSWORD_ALGORITHM_ATTRIBUTE);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException("password attribute types not found");
		}

		String seed = subject.getUserId();
		String hashValue;
		try {
			hashValue = hash(password, seed, defaultHashingAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new EJBException(
					"Could not find the default password hashing algorithm: "
							+ defaultHashingAlgorithm);
		}
		try {
			Password passwordAttribute = getPasswordAttribute(subject
					.getUserId());

			passwordAttribute.hash.setStringValue(hashValue);
			passwordAttribute.seed.setStringValue(seed);
			passwordAttribute.algorithm.setStringValue(defaultHashingAlgorithm);
		} catch (DeviceNotFoundException e) {
			this.attributeDAO.addAttribute(passwordHashAttributeType, subject,
					hashValue);
			this.attributeDAO.addAttribute(passwordSeedAttributeType, subject,
					seed);
			this.attributeDAO.addAttribute(passwordAlgorithmAttributeType,
					subject, defaultHashingAlgorithm);
		}
	}

	public boolean validatePassword(SubjectEntity subject, String password)
			throws DeviceNotFoundException {

		// get current password
		Password expectedPassword = getPasswordAttribute(subject.getUserId());
		String expectedPasswordHash = expectedPassword.hash.getStringValue();
		String seed = expectedPassword.seed.getStringValue();
		String algorithm = expectedPassword.algorithm.getStringValue();

		// calculate hash
		String givenPasswordHash;
		try {
			givenPasswordHash = hash(password, seed, algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new EJBException("Password hashing algorithm not found: "
					+ algorithm);
		}

		// compare hash
		if (expectedPasswordHash.equals(givenPasswordHash)) {
			// update hash to new default
			setPasswordWithForce(subject, password);
			return true;
		}
		return false;
	}

	private Password getPasswordAttribute(String login)
			throws DeviceNotFoundException {
		AttributeEntity passwordHashAttribute = this.attributeDAO
				.findAttribute(SafeOnlineConstants.PASSWORD_HASH_ATTRIBUTE,
						login);
		AttributeEntity passwordSeedAttribute = this.attributeDAO
				.findAttribute(SafeOnlineConstants.PASSWORD_SEED_ATTRIBUTE,
						login);
		AttributeEntity passwordAlgorithmAttribute = this.attributeDAO
				.findAttribute(
						SafeOnlineConstants.PASSWORD_ALGORITHM_ATTRIBUTE, login);
		if (null == passwordHashAttribute || null == passwordSeedAttribute
				|| null == passwordAlgorithmAttribute) {
			throw new DeviceNotFoundException();
		}
		String hash = passwordHashAttribute.getStringValue();
		String seed = passwordSeedAttribute.getStringValue();
		String algorithm = passwordAlgorithmAttribute.getStringValue();
		if (null == hash || null == seed || null == algorithm) {
			throw new DeviceNotFoundException();
		}

		return new Password(passwordHashAttribute, passwordSeedAttribute,
				passwordAlgorithmAttribute);
	}

	public boolean isPasswordConfigured(SubjectEntity subject) {
		try {
			getPasswordAttribute(subject.getUserId());
		} catch (DeviceNotFoundException e) {
			return false;
		}
		return true;
	}

	private static class Password {
		public AttributeEntity hash;
		public AttributeEntity seed;
		public AttributeEntity algorithm;

		public Password(AttributeEntity hash, AttributeEntity seed,
				AttributeEntity algorithm) {
			this.hash = hash;
			this.seed = seed;
			this.algorithm = algorithm;
		}
	}

	private static String hash(String input, String seed, String algorithm)
			throws NoSuchAlgorithmException {

		String toHash = input + seed;
		byte[] plainText = null;

		try {
			plainText = toHash.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new EJBException(
					"Unsupported encoding in password hash function");
		}

		MessageDigest messageDigest = MessageDigest.getInstance(algorithm,
				new BouncyCastleProvider());

		messageDigest.update(plainText);
		String digestAsString = new sun.misc.BASE64Encoder()
				.encode(messageDigest.digest());

		return digestAsString;
	}

}
