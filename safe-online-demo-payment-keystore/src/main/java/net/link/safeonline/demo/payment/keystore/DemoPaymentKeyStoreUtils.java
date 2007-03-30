/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.keystore;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Enumeration;

public class DemoPaymentKeyStoreUtils {

	public static final String KEYSTORE_RESOURCE = "safe-online-demo-payment-keystore.jks";

	public static PrivateKeyEntry getPrivateKeyEntry() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		InputStream keyStoreInputStream = classLoader
				.getResourceAsStream(KEYSTORE_RESOURCE);
		if (null == keyStoreInputStream) {
			throw new RuntimeException("keystore not found: "
					+ KEYSTORE_RESOURCE);
		}
		PrivateKeyEntry privateKeyEntry = loadPrivateKeyEntry("jks",
				keyStoreInputStream, "secret", "secret");
		return privateKeyEntry;
	}

	private static PrivateKeyEntry loadPrivateKeyEntry(String keystoreType,
			InputStream keyStoreInputStream, String keyStorePassword,
			String keyEntryPassword) {
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance(keystoreType);
		} catch (Exception e) {
			throw new RuntimeException("keystore instance not available: "
					+ e.getMessage(), e);
		}
		try {
			keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());
		} catch (Exception e) {
			throw new RuntimeException(
					"keystore load error: " + e.getMessage(), e);
		}
		Enumeration<String> aliases;
		try {
			aliases = keyStore.aliases();
		} catch (KeyStoreException e) {
			throw new RuntimeException("could not get aliases: "
					+ e.getMessage(), e);
		}
		if (!aliases.hasMoreElements()) {
			throw new RuntimeException("keystore is empty");
		}
		String alias = aliases.nextElement();
		try {
			if (!keyStore.isKeyEntry(alias)) {
				throw new RuntimeException("not key entry: " + alias);
			}
		} catch (KeyStoreException e) {
			throw new RuntimeException("key store error: " + e.getMessage(), e);
		}
		try {
			PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry) keyStore
					.getEntry(alias, new KeyStore.PasswordProtection(
							keyEntryPassword.toCharArray()));
			return privateKeyEntry;
		} catch (Exception e) {
			throw new RuntimeException("error retrieving key: "
					+ e.getMessage(), e);
		}
	}
}
