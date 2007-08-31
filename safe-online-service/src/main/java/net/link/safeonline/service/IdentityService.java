/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.sdk.KeyStoreUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * JMX bean that manages the identity of the SafeOnline instance. This identity
 * is used for signing of SAML tokens.
 * 
 * <p>
 * This identity service can be extended later on when other types of identity
 * tokens must be supported. For example: PKCS#12 resource and file key stores
 * or HSMs via PKCS#11 drivers.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class IdentityService extends ServiceMBeanSupport implements
		IdentityServiceMBean {

	private static final Log LOG = LogFactory.getLog(IdentityService.class);

	public IdentityService() {
		LOG.debug("construction");
	}

	private String keyStoreResource;

	private String keyStorePassword;

	private String keyStoreType;

	private PrivateKey privateKey;

	private PublicKey publicKey;

	public void loadKeyPair() {
		LOG.debug("load private key");
		if (null == this.keyStoreResource) {
			throw new RuntimeException("no key store resource set");
		}
		if (null == this.keyStorePassword) {
			throw new RuntimeException("no key store password set");
		}
		if (null == this.keyStoreType) {
			throw new RuntimeException("no key store type set");
		}

		Thread currenThread = Thread.currentThread();
		ClassLoader classLoader = currenThread.getContextClassLoader();
		InputStream keyStoreInputStream = classLoader
				.getResourceAsStream(this.keyStoreResource);
		if (null == keyStoreInputStream) {
			throw new RuntimeException("keystore resource not found: "
					+ this.keyStoreResource);
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
		this.publicKey = privateKeyEntry.getCertificate().getPublicKey();
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

}