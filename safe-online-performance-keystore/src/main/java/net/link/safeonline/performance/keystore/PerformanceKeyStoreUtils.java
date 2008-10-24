/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.keystore;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Enumeration;


/**
 * <h2>{@link PerformanceKeyStoreUtils}<br>
 * <sub>Access to performance-application keys from the keystore.</sub></h2>
 * 
 * <p>
 * The private key and certificate will be lazily loaded from the default keystore file if not yet available upon the first request for it.
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class PerformanceKeyStoreUtils {

    private static PrivateKeyEntry privateKeyEntry;


    private PerformanceKeyStoreUtils() {

        // empty
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        if (privateKeyEntry == null) {
            privateKeyEntry = loadPrivateKeyEntry("jks", "secret", "secret");
        }

        return privateKeyEntry;
    }

    private static PrivateKeyEntry loadPrivateKeyEntry(String keystoreType, String keyStorePassword, String keyEntryPassword) {

        /* Find the keystore. */
        String keyStoreResource = "safe-online-performance-keystore.jks";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream keyStoreInputStream = classLoader.getResourceAsStream(keyStoreResource);
        if (null == keyStoreInputStream) {
            throw new RuntimeException("keystore not found: " + keyStoreResource);
        }

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(keystoreType);
        } catch (Exception e) {
            throw new RuntimeException("keystore instance not available: " + e.getMessage(), e);
        }

        /* Open the keystore and find the key entry. */
        try {
            keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("keystore load error: " + e.getMessage(), e);
        }
        Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        } catch (KeyStoreException e) {
            throw new RuntimeException("could not get aliases: " + e.getMessage(), e);
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

        /* Get the private key entry. */
        try {
            return (PrivateKeyEntry) keyStore.getEntry(alias, new KeyStore.PasswordProtection(keyEntryPassword.toCharArray()));
        } catch (Exception e) {
            throw new RuntimeException("error retrieving key: " + e.getMessage(), e);
        }
    }
}
