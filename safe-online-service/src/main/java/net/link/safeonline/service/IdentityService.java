/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import net.link.safeonline.sdk.KeyStoreUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JMX bean that manages the identity of the SafeOnline instance. This identity
 * is used for signing of SAML tokens.
 * 
 * <p>
 * This identity service can be extended later on when other types of identity
 * tokens must be supported. For example: HSMs via PKCS#11 drivers.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class IdentityService implements IdentityServiceMBean {

	private static final Log LOG = LogFactory.getLog(IdentityService.class);

	public IdentityService() {
		LOG.debug("construction");
	}

	private String keyStoreResource;

	private String keyStoreFile;

	private String keyStorePassword;

	private String keyStoreType;

	private PrivateKey privateKey;

	private PublicKey publicKey;

	private X509Certificate certificate;

	public void loadKeyPair() {
		LOG.debug("load private key");
		if (null == this.keyStoreResource && null == this.keyStoreFile) {
			throw new RuntimeException("no key store resource or file set");
		}
		if (null == this.keyStorePassword) {
			throw new RuntimeException("no key store password set");
		}
		if (null == this.keyStoreType) {
			throw new RuntimeException("no key store type set");
		}

		InputStream keyStoreInputStream;
		if (null != this.keyStoreResource) {
			Thread currenThread = Thread.currentThread();
			ClassLoader classLoader = currenThread.getContextClassLoader();
			keyStoreInputStream = classLoader
					.getResourceAsStream(this.keyStoreResource);
			if (null == keyStoreInputStream) {
				throw new RuntimeException("keystore resource not found: "
						+ this.keyStoreResource);
			}
		} else {
			try {
				keyStoreInputStream = new FileInputStream(this.keyStoreFile);
			} catch (FileNotFoundException e) {
				throw new RuntimeException("keystore file not found: "
						+ this.keyStoreFile);
			}
		}

		PrivateKeyEntry privateKeyEntry;
		try {
			privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry(
					this.keyStoreType, keyStoreInputStream,
					this.keyStorePassword, this.keyStorePassword);
		} finally {
			IOUtils.closeQuietly(keyStoreInputStream);
		}
		this.privateKey = privateKeyEntry.getPrivateKey();
		this.certificate = (X509Certificate) privateKeyEntry.getCertificate();
		this.publicKey = this.certificate.getPublicKey();
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public void setKeyStoreResource(String keyStoreResource) {
		LOG.debug("set key store resource: " + keyStoreResource);
		this.keyStoreResource = keyStoreResource;
	}

	public String getKeyStorePassword() {
		return this.keyStorePassword;
	}

	public String getKeyStoreResource() {
		LOG.debug("get key store resource: " + this.keyStoreResource);
		return this.keyStoreResource;
	}

	public String getKeyStoreType() {
		return this.keyStoreType;
	}

	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	public PrivateKey getPrivateKey() {
		if (null == this.privateKey) {
			loadKeyPair();
		}
		return this.privateKey;
	}

	public PublicKey getPublicKey() {
		if (null == this.publicKey) {
			loadKeyPair();
		}
		return this.publicKey;
	}

	public String getKeyStoreFile() {
		return this.keyStoreFile;
	}

	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	public X509Certificate getCertificate() {
		if (null == this.certificate) {
			loadKeyPair();
		}
		return this.certificate;
	}
}
