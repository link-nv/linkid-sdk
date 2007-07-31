/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Enumeration;

/**
 * Utility class to load keystore key material.
 * 
 * @author fcorneli
 * 
 */
public class KeyStoreUtils {

	private KeyStoreUtils() {
		// empty
	}

	/**
	 * Loads a private key entry from a input stream.
	 * 
	 * <p>
	 * The supported types of keystores depend on the configured java security
	 * providers. Example: "pkcs12".
	 * </p>
	 * 
	 * <p>
	 * A good alternative java security provider is <a
	 * href="http://www.bouncycastle.org/">Bouncy Castle</a>.
	 * </p>
	 * 
	 * @param keystoreType
	 *            the type of the keystore.
	 * @param keyStoreInputStream
	 * @param keyStorePassword
	 * @param keyEntryPassword
	 * @return
	 */
	public static PrivateKeyEntry loadPrivateKeyEntry(String keystoreType,
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
